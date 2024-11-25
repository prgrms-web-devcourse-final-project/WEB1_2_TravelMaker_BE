package edu.example.wayfarer.entity;



import edu.example.wayfarer.util.RandomStringGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Room {

    @Id
    private String roomId;  // 랜덤 문자열

    private String title;
    private String country;
    private LocalDate startDate;
    private LocalDate endDate;
    private String roomCode;

    private String hostEmail;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room")
    private List<MemberRoom> memberRooms;

    // Room 생성시 랜덤 roomId 할당
    @PrePersist // when generating unique identifiers
    public void generateRoomIdAndRoomCode(){
        if(this.roomId == null || this.roomId.isBlank()){
            this.roomId = RandomStringGenerator.generateRandomString(8);
        }
        if (this.roomCode == null || this.roomCode.isBlank()) {
            this.roomCode = RandomStringGenerator.generateRandomString(8);
        }
    }

    public void changeTitle(String title){
        this.title = title;
    }

    public void changeCountry(String country){
        this.country = country;
    }

    public void changeStartDate(LocalDate startDate){
        this.startDate = startDate;
    }

    public void changeEndDate(LocalDate endDate){
        this.endDate = endDate;
    }


}
