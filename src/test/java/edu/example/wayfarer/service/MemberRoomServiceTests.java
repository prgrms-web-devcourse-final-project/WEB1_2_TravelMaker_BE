package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.memberRoom.MemberRoomRequestDTO;
import edu.example.wayfarer.dto.memberRoom.MemberRoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomListDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MemberRoomServiceTests {

    @Autowired
    private MemberRoomService memberRoomService;

    @Test
    @Transactional
    @Commit
    public void testCreateMemberRoom() {
        MemberRoomRequestDTO memberRoomRequestDTO = new MemberRoomRequestDTO(
                "SMAZCett",
                "nyNqsifP",
                "aa@aa.com"
        );
        memberRoomService.create(memberRoomRequestDTO);
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteMemberRoom() {
        String email = "aa@aa.com";
        String roomId = "xu688Ljt";

        memberRoomService.delete(email, roomId);
    }

    @Test
    public void testListByRoomId(){
        String roomId = "xu688Ljt";
        List<MemberRoomResponseDTO> members = memberRoomService.listByRoomId(roomId);
        System.out.println(members);
    }

    @Test
    public void testListByEmail(){
        String email = "aa@aa.com";
        List<RoomListDTO> rooms = memberRoomService.listByEmail(email);
        System.out.println(rooms);
    }

}
