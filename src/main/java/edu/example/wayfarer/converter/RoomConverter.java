package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Room;

import java.util.ArrayList;

public class RoomConverter {

    public static Room toRoom(RoomRequestDTO roomRequestDTO) {
        return Room.builder()
                .title(roomRequestDTO.title())
                .country(roomRequestDTO.country())
                .startDate(roomRequestDTO.startDate())
                .endDate(roomRequestDTO.endDate())
                .memberRooms(new ArrayList<>())
                .build();
    }

    public static RoomListDTO toRoomListDTO(Room room) {
        return new RoomListDTO(
                room.getTitle(),
                room.getCountry(),
                room.getStartDate(),
                room.getEndDate()
        );
    }

    public static RoomResponseDTO toRoomResponseDTO(Room room) {
        return new RoomResponseDTO(
                room.getRoomId(),
                room.getTitle(),
                room.getCountry(),
                room.getStartDate(),
                room.getEndDate(),
                room.getRoomCode(),
                room.getHostEmail(),
                room.getMemberRooms().stream().map(
                        memberRoom -> memberRoom.getMember().getEmail()
                ).toList()
//                room.getUrl()
        );
    }



}
