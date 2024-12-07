package edu.example.wayfarer.dto.chatMessage;

public record ChatMessageUpdateDTO(
        Long chatMessageId,
        String email,
        String content
) {
}
