package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.memberRoom.MemberRoomForceDeleteDTO;
import edu.example.wayfarer.dto.memberRoom.MemberRoomRequestDTO;
import edu.example.wayfarer.dto.memberRoom.MemberRoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.entity.Member;

import java.util.List;

public interface MemberRoomService {

    MemberRoomResponseDTO create(MemberRoomRequestDTO memberRoomRequestDTO, String email);

    void delete(Member member, String roomId);

    List<MemberRoomResponseDTO> listByRoomId(String roomId);

    List<RoomListDTO> listByEmail(Member member);

    void forceDelete(MemberRoomForceDeleteDTO forceDeleteDTO, Member member);

}
