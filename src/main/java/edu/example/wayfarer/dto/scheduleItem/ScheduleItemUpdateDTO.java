package edu.example.wayfarer.dto.scheduleItem;

public record ScheduleItemUpdateDTO(
        Long scheduleItemId,
        String name,
        String content,
        Long previousItemId,  // 앞의 scheduleItemId
        Long nextItemId  // 뒤의 scheduleItemId
) {}
