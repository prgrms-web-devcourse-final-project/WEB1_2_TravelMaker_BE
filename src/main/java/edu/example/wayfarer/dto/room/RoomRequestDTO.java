package edu.example.wayfarer.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RoomRequestDTO {
    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String hostEmail;   // 임시, 나중에 로그인해서 알아서 받아오게 하면 될 것 같아요
}
