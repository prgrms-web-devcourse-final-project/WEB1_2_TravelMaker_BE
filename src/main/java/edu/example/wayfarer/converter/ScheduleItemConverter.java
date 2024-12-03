package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.entity.ScheduleItem;

public class ScheduleItemConverter {
    public static ScheduleItemResponseDTO toScheduleItemResponseDTO(
            ScheduleItem scheduleItem,
            int itemOrder
    ) {
        return new ScheduleItemResponseDTO(
                scheduleItem.getScheduleItemId(),
                scheduleItem.getMarker().getMarkerId(),
                scheduleItem.getName(),
                scheduleItem.getAddress(),
                scheduleItem.getContent(),
                itemOrder,
                scheduleItem.getCreatedAt(),
                scheduleItem.getUpdatedAt()
        );
    }
}
