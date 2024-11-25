package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RoomServiceTest {
    @Autowired
    private RoomService roomService;

    @Test
    @Transactional
    @Commit
    public void testCreateRoom() {
        RoomRequestDTO roomRequestDTO = new RoomRequestDTO();
        roomRequestDTO.setTitle("크리스마스 흐흐");
        roomRequestDTO.setCountry("서울");
        roomRequestDTO.setStartDate(LocalDate.of(2024,12,23));
        roomRequestDTO.setEndDate(LocalDate.of(2024,12,27));
        roomRequestDTO.setHostEmail("aa@aa.com");

        RoomResponseDTO result = roomService.create(roomRequestDTO);
        assertNotNull(result);
    }

    @Test
    public void testReadRoom(){
        String roomId = "8FQ7Cjc9";

        RoomResponseDTO result = roomService.read(roomId);
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    @Transactional
    @Commit
    public void testUpdateRoom() {
        String roomId = "TSNAnwj4";
        RoomUpdateDTO roomUpdateDTO = new RoomUpdateDTO();
        roomUpdateDTO.setRoomId(roomId);
        roomUpdateDTO.setCountry("중국");
        roomUpdateDTO.setTitle("후후탕후루를먹자");
        roomUpdateDTO.setStartDate(LocalDate.of(2025,1,1));
        roomUpdateDTO.setEndDate(LocalDate.of(2025,1,3));

        RoomResponseDTO result = roomService.update(roomUpdateDTO);
        assertNotNull(result);
        assertEquals("중국", result.getCountry());
        assertEquals("후후탕후루를먹자", result.getTitle());
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteRoom(){
        String roomId = "vTJBdpwg";
        roomService.delete(roomId);
    }
}
