package edu.example.wayfarer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.example.wayfarer.entity.enums.Color;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Marker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markerId;

    @ManyToOne
    @JoinColumn(name = "email")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @OneToOne(mappedBy = "marker", cascade = CascadeType.ALL, orphanRemoval = true)
    private ScheduleItem scheduleItem;

    private Double lat;
    private Double lng;
    private Color color;
    private Boolean confirm;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void changeConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    public void changeColor(Color color) {
        this.color = color;
    }

    public void changeScheduleItem(ScheduleItem scheduleItem) {
        this.scheduleItem = scheduleItem;
    }
}
