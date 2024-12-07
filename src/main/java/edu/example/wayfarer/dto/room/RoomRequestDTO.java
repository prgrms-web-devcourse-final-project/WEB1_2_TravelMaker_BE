package edu.example.wayfarer.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record RoomRequestDTO (
        @Schema(description = "방 제목", defaultValue = "OO여행!!") String title,
        @Schema(description = "나라", defaultValue = "프랑스") String country,
        @Schema(description = "여행 시작일", defaultValue = "2025-01-01") LocalDate startDate,
        @Schema(description = "여행 끝일", defaultValue = "2025-01-05") LocalDate endDate
){}


