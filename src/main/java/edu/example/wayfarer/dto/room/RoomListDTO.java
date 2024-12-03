package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record RoomListDTO(
        @Schema(defaultValue = "OO여행!!") String title,
        @Schema(defaultValue = "프랑스") String country,
        @Schema(defaultValue = "2025-01-01") LocalDate startDate,
        @Schema(defaultValue = "2025-01-05") LocalDate endDate
) {

}
