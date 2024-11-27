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
    public MemberRoomResponseDTO(MemberRoom memberRoom) {
        this(
                memberRoom.getMemberRoomId(),
                memberRoom.getRoom().getRoomId(),
                memberRoom.getMember().getEmail(),
                memberRoom.getColor(),
                memberRoom.getJoinDate()
        );
    }
}
