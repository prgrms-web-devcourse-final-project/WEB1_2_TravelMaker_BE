package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;


import java.time.LocalDate;
import java.util.List;

public record RoomResponseDTO(
        String roomId,
        String title,
        String country,
        LocalDate startDate,
        LocalDate endDate,
        String roomCode,
        String hostEmail,
        List<String> members,
        String url
) {

    public RoomResponseDTO(Room room) {
        this(
                room.getRoomId(),
                room.getTitle(),
                room.getCountry(),
                room.getStartDate(),
                room.getEndDate(),
                room.getRoomCode(),
                room.getHostEmail(),
                room.getMemberRooms().stream().map(
                        memberRoom -> memberRoom.getMember().getEmail()
                ).toList(),
                room.getUrl()
        );
    }

}
