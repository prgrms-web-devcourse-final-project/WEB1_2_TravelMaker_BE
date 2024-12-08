package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.memberRoom.MemberRoomResponseDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.entity.enums.Color;

public class MemberRoomConverter {

    public static MemberRoom toMemberRoom(Room room, Member member, Color color){
        return MemberRoom.builder()
                .member(member)
                .room(room)
                .color(color)
                .build();
    }

    public static MemberRoomResponseDTO toMemberRoomResponseDTO(MemberRoom memberRoom) {
        return new MemberRoomResponseDTO(
                memberRoom.getMemberRoomId(),
                memberRoom.getRoom().getRoomId(),
                memberRoom.getMember().getEmail(),
                memberRoom.getColor(),
                memberRoom.getJoinDate()
        );
    }

}
