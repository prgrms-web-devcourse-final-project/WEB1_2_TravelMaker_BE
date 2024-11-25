package edu.example.wayfarer.dto.memberRoom;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.enums.Color;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MemberRoomResponseDTO {
    private Long memberRoomId;
    private String roomId;
    private String email;
    private Color color;
    private LocalDateTime joinDate;

    public MemberRoomResponseDTO(MemberRoom memberRoom) {
        this.memberRoomId = memberRoom.getMemberRoomId();
        this.roomId = memberRoom.getRoom().getRoomId();
        this.email = memberRoom.getMember().getEmail();
        this.color = memberRoom.getColor();
        this.joinDate = memberRoom.getJoinDate();
    }
}
