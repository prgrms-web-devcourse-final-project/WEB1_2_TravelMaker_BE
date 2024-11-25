package edu.example.wayfarer.dto.memberRoom;

import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRoomRequestDTO {
    private String roomId;
    private String roomCode;
    private String email; //임시

    public MemberRoom toEntity(Member member, Room room){
        MemberRoom memberRoom = MemberRoom.builder()
                .room(room).member(member).build();

        return memberRoom;
    }

}
