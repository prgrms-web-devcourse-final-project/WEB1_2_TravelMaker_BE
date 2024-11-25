package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;

import java.util.List;

public interface ScheduleItemService {

    ScheduleItemResponseDTO read(Long scheduleItemId);

    List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId);

    ScheduleItemResponseDTO update(ScheduleItemUpdateDTO scheduleItemUpdateDTO);

    void delete(Long scheduleItemId);
}
