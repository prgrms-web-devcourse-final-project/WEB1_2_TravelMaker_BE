package edu.example.wayfarer.dto.memberRoom;


import io.swagger.v3.oas.annotations.media.Schema;

public record MemberRoomRequestDTO (
        @Schema(description = "방ID", defaultValue = "728t5EIw") String roomId,
        @Schema(description = "방CODE", defaultValue = "PBBMbFpC") String roomCode
) {}
