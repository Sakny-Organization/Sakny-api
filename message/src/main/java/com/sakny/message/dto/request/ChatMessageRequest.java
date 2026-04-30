package com.sakny.message.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload sent by the client over the STOMP channel.
 * The sender is resolved from the JWT principal, never trusted from the payload.
 */
@Data
public class ChatMessageRequest {

    @NotNull(message = "Receiver ID is required")
    @Positive(message = "Receiver ID must be a positive number")
    private Long receiverId;

    @NotBlank(message = "Message content must not be blank")
    @Size(max = 5000, message = "Message content must not exceed 5000 characters")
    private String content;
}
