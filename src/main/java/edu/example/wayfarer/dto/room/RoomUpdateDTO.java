package edu.example.wayfarer.dto.room;


import edu.example.wayfarer.entity.Member;

import java.time.LocalDate;


public record RoomUpdateDTO (
        String roomId,
        Member member,
        String title,
        String country,
        LocalDate startDate,
        LocalDate endDate
){}
