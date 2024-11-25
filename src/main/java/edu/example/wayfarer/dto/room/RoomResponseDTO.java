package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class RoomResponseDTO {

    private String roomId;
    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String roomCode;
    private String hostEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoomResponseDTO(Room room) {
        this.roomId = room.getRoomId();
        this.title = room.getTitle();
        this.country = room.getCountry();
        this.startDate = room.getStartDate();
        this.endDate = room.getEndDate();
        this.roomCode = room.getRoomCode();
        this.hostEmail = room.getHostEmail();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();

    }

}
