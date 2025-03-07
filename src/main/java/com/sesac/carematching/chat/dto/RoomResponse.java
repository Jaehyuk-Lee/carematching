package com.sesac.carematching.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomResponse {
    private Integer roomId;
    private Integer requesterUserId;
    private Integer receiverUserId; // 👈 caregiverId → receiverUserId 로 변경
    private String createdAt;
    private String otherUsername;
    private List<MessageResponse> messages;
    private String lastMessage;
    private String lastMessageDate;

    public RoomResponse(
        @NotNull Integer roomId,
        @NotNull Integer requesterUserId,
        @NotNull Integer receiverUserId, // 👈 변경된 부분
        @NotNull Instant createdAt,
        @NotNull String otherUsername,
        @NotNull List<MessageResponse> messages,
        @NotNull String lastMessage,
        @NotNull String lastMessageDate
    ) {
        this.roomId = roomId;
        this.requesterUserId = requesterUserId;
        this.receiverUserId = receiverUserId; // 👈 caregiverId → receiverUserId 로 변경
        this.otherUsername = otherUsername;
        this.createdAt = DateTimeFormatter.ISO_INSTANT.format(createdAt);
        this.messages = messages;
        this.lastMessage = lastMessage;
        this.lastMessageDate = lastMessageDate;
    }
}

