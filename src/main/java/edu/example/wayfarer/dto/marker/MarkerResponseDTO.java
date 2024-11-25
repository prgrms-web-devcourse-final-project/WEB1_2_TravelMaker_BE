package edu.example.wayfarer.dto.marker;

import java.time.LocalDateTime;

public record MarkerResponseDTO(
        Long markerId,
        String email,
        Long scheduleId,
        double lat,
        double lng,
        String color,
        Boolean confirm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}