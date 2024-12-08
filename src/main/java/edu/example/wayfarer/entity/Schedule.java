package edu.example.wayfarer.entity;

//import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

//    @Enumerated(EnumType.STRING)
//    private Days date;

    private LocalDate actualDate;

    public void changeActualDate(LocalDate actualDate) {
        this.actualDate = actualDate;
    }

}
