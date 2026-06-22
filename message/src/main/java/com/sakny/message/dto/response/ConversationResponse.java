package com.sakny.message.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {

    private Long conversationId;

    /** The other participant (not the currently authenticated user). */
    private Long otherUserId;
    private String otherUserName;
    private String otherUserPhoto;

    /** Snippet shown in the conversation list. */
    private String lastMessageContent;
    private LocalDateTime lastMessageSentAt;

    /** How many unread messages the current user has in this conversation. */
    private long unreadCount;

    private LocalDateTime updatedAt;
}
