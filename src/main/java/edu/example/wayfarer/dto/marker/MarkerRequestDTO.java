package edu.example.wayfarer.dto.marker;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record MarkerRequestDTO(
        @NotNull(message = "Email은 필수 입니다.")
        String email,

        @NotNull(message = "ScheduleId는 필수 입니다.")
        Long scheduleId,

        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        double lat,

        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        double lng
) {}

