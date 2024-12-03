package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Marker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarkerRepository extends JpaRepository<Marker, Long> {

    List<Marker> findByScheduleScheduleId(Long scheduleId);

    Optional<Marker> findByScheduleItemScheduleItemId(Long scheduleItemId);

    Boolean existsByScheduleScheduleId(Long scheduleId);

    Long countByScheduleScheduleId(Long scheduleId);

    Long countByScheduleScheduleIdAndConfirmTrue(Long scheduleId);

    @Modifying
    @Query("DELETE FROM Marker m WHERE m.schedule.scheduleId IN (" +
            "SELECT s.scheduleId FROM Schedule s WHERE s.room.roomId = :roomId)")
    void deleteByRoomId(@Param("roomId") String roomId);

}
