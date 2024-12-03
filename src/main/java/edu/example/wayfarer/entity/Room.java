package edu.example.wayfarer.entity;



import edu.example.wayfarer.util.RandomStringGenerator;
import io.micrometer.common.util.StringUtils;
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
//    private String url;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<MemberRoom> memberRooms;

    // Room 생성시 랜덤 roomId 할당
    @PrePersist
    public void generateRoomIdAndRoomCode(){
        if(StringUtils.isEmpty(roomId)){
            this.roomId = RandomStringGenerator.generateRandomString(8);
        }
        if (StringUtils.isEmpty(roomCode)) {
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
