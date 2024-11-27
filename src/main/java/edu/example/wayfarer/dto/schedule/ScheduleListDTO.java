package edu.example.wayfarer.dto.schedule;

import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;

import java.time.LocalDate;

public record ScheduleListDTO(
        Days date,
        PlanType planType,
        LocalDate actualDate
) {
    public ScheduleListDTO(Schedule schedule) {
        this(
                schedule.getDate(),
                schedule.getPlanType(),
                schedule.getActualDate()
        );
    }
}
