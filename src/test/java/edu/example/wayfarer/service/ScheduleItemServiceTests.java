package edu.example.wayfarer.service;


import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;
import java.util.List;

// 해당 테스트는 MarkerServiceTests 를 수행 후 생성된 객체를 가지고 수행합니다.
// 추후 수정 예정
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScheduleItemServiceTests {
    @Autowired
    private ScheduleItemService scheduleItemService;

    @Test
    @Order(1)
    public void testRead() {
        Long scheduleItemId = 1L;

        ScheduleItemResponseDTO scheduleItemResponseDTO = scheduleItemService.read(scheduleItemId);

        System.out.println(scheduleItemResponseDTO);
    }

    @Test
    @Order(2)
    public void testGetListByScheduleId() {
        Long scheduleId = 1L;

        List<ScheduleItemResponseDTO> results = scheduleItemService.getListBySchedule(scheduleId);

        System.out.println(results);
    }

    @Test
    @Order(3)
    public void testUpdate() {
        ScheduleItemUpdateDTO updateDTO = new ScheduleItemUpdateDTO(
                1L,
                "Updated Name",
                "Updated Content",
                null,
                null
        );

        ScheduleItemResponseDTO result = scheduleItemService.update(updateDTO);
        System.out.println(result);
    }

    @Test
    @Order(4)
    public void testDelete() {
        Long ScheduleItemId = 1L;
        scheduleItemService.delete(ScheduleItemId);
    }
}
