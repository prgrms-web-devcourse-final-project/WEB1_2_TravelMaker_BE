package edu.example.wayfarer.dto.schedule;

import edu.example.wayfarer.entity.Schedule;
//import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;

import java.time.LocalDate;

public record ScheduleListDTO(
//        Days date,
        Long scheduleId,
        PlanType planType,
        LocalDate actualDate
) {
    public ScheduleListDTO(Schedule schedule) {
        this(
//                schedule.getDate(),
                schedule.getScheduleId(),
                schedule.getPlanType(),
                schedule.getActualDate()
        );
    }
}
