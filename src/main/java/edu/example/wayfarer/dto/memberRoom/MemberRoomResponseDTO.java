package edu.example.wayfarer.dto.memberRoom;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.enums.Color;

import java.time.LocalDateTime;


public record MemberRoomResponseDTO(
        Long memberRoomId,
        String roomId,
        String email,
        Color color,
        LocalDateTime joinDate
) {
}
