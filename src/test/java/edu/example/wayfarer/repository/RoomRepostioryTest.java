package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Room;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestPropertySource(locations = "classpath:application-test.properties")
public class RoomRepostioryTest {

    @Autowired
    private RoomRepository roomRepository;

//    @BeforeEach
//    void setup(){
//        roomRepository.deleteAll(); // 기존 방 삭제
//
//    }

    @Test
    @DisplayName("방 생성 테스트")
    @Commit
    @Transactional
    public void testCreateRoom(){

        Room room = Room.builder()
                .country("HongKong")
                .hostEmail("zz@a.com")
                .startDate(LocalDate.of(2025, 1, 6))
                .endDate(LocalDate.of(2025, 1, 10))
                .title("에타기다려")
                .build();

        Room savedRoom = roomRepository.save(room);
        assertNotNull(room.getRoomId());
        assertThat(savedRoom).isNotNull();

    }

    @Test
    public void testReadRoom(){
        String roomId = "NZQxJBkIToYgEKVTnrO6";

        Room room = roomRepository.findById(roomId).orElse(null);

        assertNotNull(room);
        assertThat(room.getRoomId()).isEqualTo(roomId);
        System.out.println(room.getRoomCode() + " " + room.getTitle());
    }

    @Test
    @Transactional
    @Commit
    public void testUpdateRoom(){
        String roomId = "NZQxJBkIToYgEKVTnrO6";
        String country = "USA";
        String title = "미국기행";

        Optional<Room> foundRoom = roomRepository.findById(roomId);
        assertTrue(foundRoom.isPresent());
        Room room = foundRoom.get();

        room.changeCountry(country);
        room.changeTitle(title);

        assertEquals(country, room.getCountry());
        assertEquals(title, room.getTitle());
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteRoom(){
        String roomId = "NZQxJBkIToYgEKVTnrO6";
        assertTrue(roomRepository.findById(roomId).isPresent());

        roomRepository.deleteById(roomId);
        assertFalse(roomRepository.findById(roomId).isPresent());
    }

    @Test
    public void testReadAllRoom(){
        List<Room> rooms = roomRepository.findAll();
        assertNotNull(rooms);
        System.out.println("rooms : " + rooms.get(0).getTitle() + " " + rooms.get(1).getTitle());
    }

}
