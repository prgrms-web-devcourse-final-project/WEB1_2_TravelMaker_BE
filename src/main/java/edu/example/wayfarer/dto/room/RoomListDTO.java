package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.Room;

import java.time.LocalDate;

public record RoomListDTO(
        String title,
        String country,
        LocalDate startDate,
        LocalDate endDate
) {
    public RoomListDTO(Room room) {
        this(
                room.getTitle(),
                room.getCountry(),
                room.getStartDate(),
                room.getEndDate()
        );
    }
}
