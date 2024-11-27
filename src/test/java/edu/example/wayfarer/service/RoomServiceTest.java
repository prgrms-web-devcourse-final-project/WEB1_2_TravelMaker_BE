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
        RoomRequestDTO roomRequestDTO = new RoomRequestDTO(
                "고향",
                "캐나다",
                LocalDate.of(2024, 11, 29),
                LocalDate.of(2024, 11, 30),
                "jj@jj.com" // 임시 이메일
        );
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
        RoomUpdateDTO roomUpdateDTO = new RoomUpdateDTO(
                "TSNAnwj4", // roomId
                "후후탕후루를먹자", // title
                "중국", // country
                LocalDate.of(2025, 1, 1), // startDate
                LocalDate.of(2025, 1, 3)  // endDate
        );
        RoomResponseDTO result = roomService.update(roomUpdateDTO);
        assertNotNull(result);
        assertEquals("중국", result.country());
        assertEquals("후후탕후루를먹자", result.title());
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteRoom(){
        String roomId = "vTJBdpwg";
        roomService.delete(roomId);
    }
}
