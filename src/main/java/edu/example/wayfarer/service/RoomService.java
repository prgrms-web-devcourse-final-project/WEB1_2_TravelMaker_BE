package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.entity.Member;

import java.util.List;

public interface RoomService {

    RoomResponseDTO create(RoomRequestDTO roomRequestDTO);
    RoomResponseDTO read(String roomId);
    RoomResponseDTO update(RoomUpdateDTO roomUpdateDTO);
    void delete(Member member, String roomId);
//    List<RoomListDTO> getList();
}
