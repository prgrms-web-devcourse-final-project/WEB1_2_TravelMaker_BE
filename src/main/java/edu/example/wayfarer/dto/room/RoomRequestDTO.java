package edu.example.wayfarer.dto.room;

import java.time.LocalDate;

public record RoomRequestDTO (
        String title,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String hostEmail   // 임시, 나중에 로그인해서 알아서 받아오게 하면 될 것 같아요
){}


