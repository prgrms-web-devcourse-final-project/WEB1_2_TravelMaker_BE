package edu.example.wayfarer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Double itemOrder;

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

    public void changeItemOrder(Double itemOrder) {
        this.itemOrder = itemOrder;
    }

}
