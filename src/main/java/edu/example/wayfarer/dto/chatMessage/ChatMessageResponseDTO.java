package edu.example.wayfarer.dto.chatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponseDTO(
        String roomId,
        String email,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
