package com.sakny.message.controller;

import com.sakny.message.dto.request.ChatMessageRequest;
import com.sakny.message.dto.request.SendMessageRequest;
import com.sakny.message.dto.response.MessageResponse;
import com.sakny.message.service.MessageService;
import com.sakny.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

/**
 * Handles STOMP messages sent by clients.
 *
 * Client-side usage (SockJS + STOMP):
 *
 *   const socket = new SockJS('/ws');
 *   const stompClient = Stomp.over(socket);
 *   stompClient.connect({ Authorization: 'Bearer <token>' }, () => {
 *       // Subscribe to your personal queue
 *       stompClient.subscribe('/user/queue/messages', (frame) => {
 *           const msg = JSON.parse(frame.body);
 *           // render message
 *       });
 *
 *       // Send a message
 *       stompClient.send('/app/chat.send', {}, JSON.stringify({
 *           receiverId: 42,
 *           content: 'Hello!'
 *       }));
 *   });
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    /**
     * Destination: /app/chat.send
     *
     * Receives a chat message from the authenticated sender,
     * persists it, and pushes it to the receiver's queue
     * (/user/{receiverId}/queue/messages).
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
        @AuthenticationPrincipal User sender,
        @Valid @Payload ChatMessageRequest request
    ) {
        SendMessageRequest serviceRequest = new SendMessageRequest();
        serviceRequest.setReceiverId(request.getReceiverId());
        serviceRequest.setContent(request.getContent());

        messageService.sendMessage(sender.getId(), serviceRequest);
        // The service layer calls SimpMessagingTemplate to push to the receiver.
    }
}
