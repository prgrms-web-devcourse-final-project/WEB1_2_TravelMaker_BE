package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.schedule.ScheduleListDTO;
import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Override
    public List<ScheduleListDTO> getScheduleListByRoomId(String roomId) {
        List<Schedule> schedules = scheduleRepository.findByRoom_RoomId(roomId);

        return schedules.stream()
                .map(ScheduleListDTO::new)
                .toList();
    }
}
