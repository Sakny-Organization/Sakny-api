package com.sakny.message.controller;

import com.sakny.common.dto.ApiResponse;
import com.sakny.message.dto.request.SendMessageRequest;
import com.sakny.message.dto.response.ConversationResponse;
import com.sakny.message.dto.response.MessageResponse;
import com.sakny.message.service.MessageService;
import com.sakny.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messaging", description = "Endpoints for managing conversations and private messages between users")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Get conversations", description = "Returns paginated conversations for the current user, each with the last-message snippet and unread count.")
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<ConversationResponse>>> getConversations(
        @AuthenticationPrincipal User user,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ConversationResponse> conversations = messageService.getConversations(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @Operation(summary = "Get message history", description = "Returns paginated message history with a specific user (newest first). Also marks received messages as read.")
    @GetMapping("/{otherUserId}")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessageHistory(
        @AuthenticationPrincipal User user,
        @Parameter(description = "ID of the other user in the conversation") @PathVariable Long otherUserId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<MessageResponse> messages = messageService.getMessageHistory(
            user.getId(), otherUserId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "Send message (REST fallback)", description = "REST fallback to send a message. This mirrors the primary WebSocket channel.")
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse response = messageService.sendMessage(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Mark message as read", description = "Marks a single message as read. Only the receiver of the message can perform this action.")
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<MessageResponse>> markAsRead(
        @AuthenticationPrincipal User user,
        @Parameter(description = "ID of the message to mark as read") @PathVariable Long messageId
    ) {
        MessageResponse response = messageService.markAsRead(user.getId(), messageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
