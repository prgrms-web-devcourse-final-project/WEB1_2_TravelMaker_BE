package edu.example.wayfarer.dto.room;

import edu.example.wayfarer.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomListDTO {

    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;

    public RoomListDTO(Room room){
        this.title = room.getTitle();
        this.country = room.getCountry();
        this.startDate = room.getStartDate();
        this.endDate = room.getEndDate();
    }

}
