package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.memberRoom.MemberRoomRequestDTO;
import edu.example.wayfarer.dto.memberRoom.MemberRoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.repository.MemberRepository;
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
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Commit
    public void testCreateMemberRoom() {
        String email = "aa@aa.com";
        MemberRoomRequestDTO memberRoomRequestDTO = new MemberRoomRequestDTO(
                "728t5EIw",
                "PBBMbFpC"
        );
        memberRoomService.create(memberRoomRequestDTO, email);
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteMemberRoom() {
        String email = "aa@aa.com";
        Member member = memberRepository.findByEmail(email).get();
        String roomId = "xu688Ljt";

        memberRoomService.delete(member, roomId);
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
        Member member = memberRepository.findByEmail(email).get();
        List<RoomListDTO> rooms = memberRoomService.listByEmail(member);
        System.out.println(rooms);
    }

}
