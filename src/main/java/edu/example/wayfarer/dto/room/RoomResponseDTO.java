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
        List<String> members
//        String url
) {
}
