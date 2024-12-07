package edu.example.wayfarer.dto.chatMessage;

import edu.example.wayfarer.entity.MemberRoom;
import io.swagger.v3.oas.annotations.media.Schema;

public record ChatMessageRequestDTO(
        String roomId,
        String email,
        String content,
        String timestamp
) {
}
