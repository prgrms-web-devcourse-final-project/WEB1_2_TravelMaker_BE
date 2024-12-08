package edu.example.wayfarer.dto.marker;

import java.time.LocalDateTime;

public record MarkerResponseDTO(
        Long markerId,
        String email,
        String profileImage,
        Long scheduleId,
        double lat,
        double lng,
        String color,
        Boolean confirm,
        Integer itemOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}