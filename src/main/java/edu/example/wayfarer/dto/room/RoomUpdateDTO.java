package edu.example.wayfarer.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RoomUpdateDTO {
    private String roomId;
    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
}
