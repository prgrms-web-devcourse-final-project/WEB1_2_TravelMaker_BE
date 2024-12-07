package edu.example.wayfarer.entity;

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
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleItemId;

    @OneToOne
    @JoinColumn(name = "marker_id")
    private Marker marker;

    private String name;
    private String address;
    private String content;

    @ManyToOne
    @JoinColumn(name = "previous_item_id")
    private ScheduleItem previousItem;  // 이전 항목

    @ManyToOne
    @JoinColumn(name = "next_item_id")
    private ScheduleItem nextItem;  // 다음 항목

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void changeName(String name) {
        this.name = name;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changePreviousItem(ScheduleItem previousItem) {
        this.previousItem = previousItem;
    }

    public void changeNextItem(ScheduleItem nextItem) {
        this.nextItem = nextItem;
    }
}
