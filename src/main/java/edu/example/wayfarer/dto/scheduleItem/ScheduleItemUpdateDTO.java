package edu.example.wayfarer.dto.scheduleItem;

public record ScheduleItemUpdateDTO(
        Long scheduleItemId,
        String name,
        String content
) {}
