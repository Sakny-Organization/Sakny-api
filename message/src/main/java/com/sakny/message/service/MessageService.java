package com.sakny.message.service;

import com.sakny.message.dto.request.SendMessageRequest;
import com.sakny.message.dto.response.ConversationResponse;
import com.sakny.message.dto.response.MessageResponse;
import com.sakny.message.dto.response.UnreadCountResponse;
import com.sakny.message.entity.Conversation;
import com.sakny.message.entity.Message;
import com.sakny.message.mapper.MessageMapper;
import com.sakny.message.repository.ConversationRepository;
import com.sakny.message.repository.MessageRepository;
import com.sakny.user.entity.User;
import com.sakny.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository      messageRepository;
    private final UserRepository         userRepository;
    private final MessageMapper          messageMapper;
    private final SimpMessagingTemplate  messagingTemplate;

    // ------------------------------------------------------------------ //
    //  Conversation list
    // ------------------------------------------------------------------ //

    /**
     * Returns all conversations for the current user, enriched with the
     * last-message snippet and unread count.
     */
    public List<ConversationResponse> getConversations(Long currentUserId) {
        return conversationRepository.findAllByUserId(currentUserId)
            .stream()
            .map(conv -> buildConversationResponse(conv, currentUserId))
            .toList();
    }

    /**
     * Paginated variant — prefer this for large conversation lists.
     */
    public Page<ConversationResponse> getConversations(Long currentUserId, Pageable pageable) {
        return conversationRepository.findAllByUserIdPageable(currentUserId, pageable)
            .map(conv -> buildConversationResponse(conv, currentUserId));
    }

    // ------------------------------------------------------------------ //
    //  Message history
    // ------------------------------------------------------------------ //

    /**
     * Returns paginated message history between the current user and another user.
     * Also marks all unread messages sent to the current user as read.
     */
    @Transactional
    public Page<MessageResponse> getMessageHistory(Long currentUserId, Long otherUserId, Pageable pageable) {
        Conversation conversation = findOrThrow(currentUserId, otherUserId);

        // Side-effect: mark messages as read when the receiver fetches history
        int marked = messageRepository.markAllAsReadInConversation(conversation.getId(), currentUserId);
        if (marked > 0) {
            log.debug("Marked {} messages as read in conversation {} for user {}", marked, conversation.getId(), currentUserId);
        }

        return messageRepository
            .findByConversationId(conversation.getId(), pageable)
            .map(messageMapper::toResponse);
    }

    // ------------------------------------------------------------------ //
    //  Send message  (REST + WebSocket shared logic)
    // ------------------------------------------------------------------ //

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("You cannot send a message to yourself");
        }

        User sender   = getUserOrThrow(senderId);
        User receiver = getUserOrThrow(request.getReceiverId());

        Conversation conversation = findOrCreateConversation(sender, receiver);

        Message message = Message.builder()
            .conversation(conversation)
            .sender(sender)
            .receiver(receiver)
            .content(request.getContent())
            .build();

        message = messageRepository.save(message);
        MessageResponse response = messageMapper.toResponse(message);

        // 1. Push the full message payload to the receiver's personal queue
        messagingTemplate.convertAndSendToUser(
            String.valueOf(receiver.getId()),
            "/queue/messages",
            response
        );

        // 2. Push a lightweight unread-count badge update so the UI can
        //    refresh the conversation list indicator without a REST call
        long unread = messageRepository
            .countUnreadByConversationIdAndReceiverId(conversation.getId(), receiver.getId());

        messagingTemplate.convertAndSendToUser(
            String.valueOf(receiver.getId()),
            "/queue/unread-count",
            UnreadCountResponse.builder()
                .conversationId(conversation.getId())
                .unreadCount(unread)
                .build()
        );

        log.debug("Message {} sent from user {} to user {} in conversation {}",
            message.getId(), senderId, receiver.getId(), conversation.getId());

        return response;
    }

    // ------------------------------------------------------------------ //
    //  Mark single message as read
    // ------------------------------------------------------------------ //

    @Transactional
    public MessageResponse markAsRead(Long currentUserId, Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found: " + messageId));

        if (!message.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only mark your own received messages as read");
        }

        if (!message.isRead()) {
            message.setRead(true);
            message = messageRepository.save(message);
        }

        return messageMapper.toResponse(message);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private Conversation findOrCreateConversation(User userA, User userB) {
        Long minId = Math.min(userA.getId(), userB.getId());
        Long maxId = Math.max(userA.getId(), userB.getId());

        return conversationRepository.findByParticipants(minId, maxId)
            .orElseGet(() -> {
                User participantOne = userA.getId().equals(minId) ? userA : userB;
                User participantTwo = userA.getId().equals(maxId) ? userA : userB;

                Conversation conv = Conversation.builder()
                    .participantOne(participantOne)
                    .participantTwo(participantTwo)
                    .build();

                return conversationRepository.save(conv);
            });
    }

    private Conversation findOrThrow(Long userAId, Long userBId) {
        Long minId = Math.min(userAId, userBId);
        Long maxId = Math.max(userAId, userBId);

        return conversationRepository.findByParticipants(minId, maxId)
            .orElseThrow(() -> new EntityNotFoundException(
                "No conversation found between users " + userAId + " and " + userBId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private ConversationResponse buildConversationResponse(Conversation conv, Long currentUserId) {
        boolean currentIsOne = conv.getParticipantOne().getId().equals(currentUserId);
        User other = currentIsOne ? conv.getParticipantTwo() : conv.getParticipantOne();

        var lastMsgOpt = messageRepository.findLatestByConversationId(conv.getId());
        long unread    = messageRepository.countUnreadByConversationIdAndReceiverId(conv.getId(), currentUserId);

        return ConversationResponse.builder()
            .conversationId(conv.getId())
            .otherUserId(other.getId())
            .otherUserName(other.getName())
            .lastMessageContent(lastMsgOpt.map(Message::getContent).orElse(null))
            .lastMessageSentAt(lastMsgOpt.map(Message::getSentAt).orElse(null))
            .unreadCount(unread)
            .updatedAt(conv.getUpdatedAt())
            .build();
    }
}
