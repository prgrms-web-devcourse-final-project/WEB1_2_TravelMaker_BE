package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Marker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkerRepository extends JpaRepository<Marker, Long> {

    List<Marker> findBySchedule_ScheduleId(Long scheduleId);

    Optional<Marker> findByScheduleItem_ScheduleItemId(Long scheduleItemId);

    Boolean existsBySchedule_ScheduleId(Long scheduleId);
}
