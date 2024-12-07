package edu.example.wayfarer.dto.chatMessage;

import java.time.LocalDateTime;

public record ChatMessageListDTO(
        String roomId,
        String content,
        LocalDateTime createdAt
) {
}
