package edu.example.wayfarer.dto.room;


import java.time.LocalDate;


public record RoomUpdateDTO (
        String roomId,
        String title,
        String country,
        LocalDate startDate,
        LocalDate endDate
){}
