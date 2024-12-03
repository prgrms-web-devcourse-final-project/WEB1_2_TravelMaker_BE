package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.schedule.ScheduleListDTO;

import java.util.List;

public interface ScheduleService {
    List<ScheduleListDTO> getScheduleListByRoomId(String roomId);
}
