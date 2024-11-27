package edu.example.wayfarer.dto.schedule;

import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;

public record ScheduleListDTO(
        Days date,
        PlanType planType
) {
    public ScheduleListDTO(Schedule schedule) {
        this(
                schedule.getDate(),
                schedule.getPlanType()
        );
    }
}
