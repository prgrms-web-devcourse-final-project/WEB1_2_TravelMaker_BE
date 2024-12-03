package edu.example.wayfarer.dto.scheduleItem;

import java.time.LocalDateTime;

public record ScheduleItemResponseDTO(
        Long scheduleItemId,
        Long markerId,
        String name,
        String address,
        String content,
        int itemOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
