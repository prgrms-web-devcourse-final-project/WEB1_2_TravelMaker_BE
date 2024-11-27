package edu.example.wayfarer.dto.memberRoom;


public record MemberRoomRequestDTO (
        String roomId,
        String roomCode,
        String email
) {}
