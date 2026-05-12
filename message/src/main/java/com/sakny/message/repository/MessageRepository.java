package com.sakny.message.repository;

import com.sakny.message.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Paginated message history for a conversation, newest first.
     */
    @Query("""
        SELECT m FROM Message m
        JOIN FETCH m.sender
        JOIN FETCH m.receiver
        WHERE m.conversation.id = :conversationId
        ORDER BY m.sentAt DESC
    """)
    Page<Message> findByConversationId(
        @Param("conversationId") Long conversationId,
        Pageable pageable
    );

    /**
     * The most recent message in a conversation (for conversation list snippet).
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.conversation.id = :conversationId
        ORDER BY m.sentAt DESC
        LIMIT 1
    """)
    Optional<Message> findLatestByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Count unread messages sent to a specific user in a conversation.
     */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.conversation.id = :conversationId
          AND m.receiver.id = :receiverId
          AND m.isRead = false
    """)
    long countUnreadByConversationIdAndReceiverId(
        @Param("conversationId") Long conversationId,
        @Param("receiverId") Long receiverId
    );

    /**
     * Mark all unread messages in a conversation as read for a given receiver.
     */
    @Modifying
    @Query("""
        UPDATE Message m SET m.isRead = true
        WHERE m.conversation.id = :conversationId
          AND m.receiver.id = :receiverId
          AND m.isRead = false
    """)
    int markAllAsReadInConversation(
        @Param("conversationId") Long conversationId,
        @Param("receiverId") Long receiverId
    );
}
