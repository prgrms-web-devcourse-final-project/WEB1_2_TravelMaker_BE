package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.schedule.ScheduleListDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ScheduleServiceTests {
    @Autowired
    private ScheduleService scheduleService;

    @Test
    public void testList(){
        String roomId = "03TbDZWO";

        List<ScheduleListDTO> list = scheduleService.getScheduleListByRoomId(roomId);
        System.out.println(list);
    }
}
