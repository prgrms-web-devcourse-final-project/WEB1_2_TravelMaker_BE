package edu.example.wayfarer.manager;

import edu.example.wayfarer.dto.common.PageRequestDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.entity.ScheduleItem;

import java.util.List;

public interface ScheduleItemOrderManager {

    int getIndex(ScheduleItem scheduleItem);
    List<ScheduleItemResponseDTO> getOrderedItems(Long scheduleId);
    List<ScheduleItemResponseDTO> getPaginatedItems(Long scheduleId, PageRequestDTO pageRequestDTO);
    void updateOrder(ScheduleItem scheduleItem, Long previousItemId, Long nextItemId);
    void detachItem(ScheduleItem scheduleItem);
}
