package edu.example.wayfarer.dto.room;


import edu.example.wayfarer.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


public record RoomUpdateDTO (
        @Schema(description = "수정할 방ID", defaultValue = "728t5EIw") String roomId,
        @Schema(description = "수정할 제목", defaultValue = "OO로 떠나자") String title,
        @Schema(description = "수정할 나라", defaultValue = "독일") String country,
        @Schema(description = "수정할 여행 시작일", defaultValue = "2025-02-01") LocalDate startDate,
        @Schema(description = "수정할 여행 끝일", defaultValue = "2025-02-03") LocalDate endDate
){}
