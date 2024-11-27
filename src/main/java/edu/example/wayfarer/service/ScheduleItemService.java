package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.common.PageRequestDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScheduleItemService {

    ScheduleItemResponseDTO read(Long scheduleItemId);

    List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId);

    ScheduleItemResponseDTO update(ScheduleItemUpdateDTO scheduleItemUpdateDTO);

    void delete(Long scheduleItemId);

    ScheduleItemResponseDTO readByMarkerId(Long markerId);

    Page<ScheduleItemResponseDTO> getPageBySchedule(Long scheduleId, PageRequestDTO pageRequestDTO);
}
