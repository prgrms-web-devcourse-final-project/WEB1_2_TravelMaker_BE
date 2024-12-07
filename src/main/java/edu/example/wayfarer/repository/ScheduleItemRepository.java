package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.ScheduleItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    List<ScheduleItem> findByMarkerScheduleScheduleId(Long scheduleId);

    Page<ScheduleItem> findByMarkerScheduleScheduleId(Long scheduleId, Pageable pageable);

    Optional<ScheduleItem> findByMarkerMarkerId(Long markerId);

    Boolean existsByMarkerMarkerId(Long markerId);

    // scheduleId를 기준 시작 아이템을 조회
    Optional<ScheduleItem> findFirstByMarkerScheduleScheduleIdAndPreviousItemIsNull(Long scheduleId);

    // scheduleId를 기준 마지막 아이템을 조회
    Optional<ScheduleItem> findLastByMarkerScheduleScheduleIdAndNextItemIsNull(Long scheduleId);

    @Query("""
        SELECT COUNT(si)
        FROM ScheduleItem si
        WHERE si.marker.schedule.scheduleId = :scheduleId
    """)
    long countByScheduleId(@Param("scheduleId") Long scheduleId);
}


