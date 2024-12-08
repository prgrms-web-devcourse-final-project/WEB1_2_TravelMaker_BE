package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.repository.MemberRepository;
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
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Commit
    public void testCreateRoom() {
        RoomRequestDTO roomRequestDTO = new RoomRequestDTO(
                "테스트 그만 want 해요",
                "testingC",
                LocalDate.of(2024, 12, 30),
                LocalDate.of(2025, 1, 02)

        );
        RoomResponseDTO result = roomService.create(roomRequestDTO, "aa@aa.com");
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
        String email = "aa@aa.com";
        RoomUpdateDTO roomUpdateDTO = new RoomUpdateDTO(
                "TSNAnwj4", // roomId
                "후후탕후루를먹자", // title
                "중국", // country
                LocalDate.of(2025, 1, 1), // startDate
                LocalDate.of(2025, 1, 3)  // endDate
        );
        RoomResponseDTO result = roomService.update(roomUpdateDTO, email);
        assertNotNull(result);
        assertEquals("중국", result.country());
        assertEquals("후후탕후루를먹자", result.title());
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteRoom(){
        String roomId = "vTJBdpwg";
        String email = "aa@aa.com";
        Member member = memberRepository.findByEmail(email).get();
        roomService.delete(member,roomId);
    }
}
