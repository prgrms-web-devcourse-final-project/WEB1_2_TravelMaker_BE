package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.Room;


import java.time.LocalDate;

public record RoomResponseDTO(
         String roomId,
         String title,
         String country,
         LocalDate startDate,
         LocalDate endDate,
         String roomCode,
         String hostEmail
) {

    public RoomResponseDTO(Room room) {
        this(
                room.getRoomId(),
                room.getTitle(),
                room.getCountry(),
                room.getStartDate(),
                room.getEndDate(),
                room.getRoomCode(),
                room.getHostEmail()
        );
    }

}
