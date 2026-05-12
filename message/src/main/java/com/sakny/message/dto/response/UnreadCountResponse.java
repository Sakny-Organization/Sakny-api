package com.sakny.message.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Lightweight payload pushed to a user's /user/queue/unread-count
 * whenever a new message arrives for them, so the UI badge stays live.
 */
@Data
@Builder
public class UnreadCountResponse {
    private Long   conversationId;
    private long   unreadCount;
}
