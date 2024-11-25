package edu.example.wayfarer.entity;

import edu.example.wayfarer.entity.enums.Color;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class MemberRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberRoomId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "email")
    private Member member;
    private Color color;

    @CreatedDate
    private LocalDateTime joinDate;
}
