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

    List<ScheduleItem> findByMarker_Schedule_ScheduleId(Long scheduleId);

    Page<ScheduleItem> findByMarker_Schedule_ScheduleId(Long scheduleId, Pageable pageable);

    Optional<ScheduleItem> findByMarker_MarkerId(Long markerId);

    Boolean existsByMarker_MarkerId(Long markerId);

    void deleteByMarker_MarkerId(Long markerId);

    // 최대 itemOrder 값 조회, null 일 경우 0 반환
    @Query("SELECT COALESCE(MAX(si.itemOrder), 0) " +
            "FROM ScheduleItem si " +
            "WHERE si.marker.schedule.scheduleId = :scheduleId")
    Double findMaxItemOrderByScheduleId(@Param("scheduleId") Long scheduleId);

    // 최소 itemOrder 값 조회, null 일 경우 0 반환
    @Query("SELECT COALESCE(MIN(si.itemOrder), 0) " +
            "FROM ScheduleItem si " +
            "WHERE si.marker.schedule.scheduleId = :scheduleId")
    Double findMinItemOrderByScheduleId(@Param("scheduleId") Long scheduleId);

    // scheduleId 로 scheduleItem 리스트 조회 및 itemOrder 로 정렬
    // 메서드명 기반 쿼리를 사용하려다보니 엄청 길어졌다 이게맞나?
    List<ScheduleItem> findByMarker_Schedule_ScheduleIdOrderByItemOrderAsc(Long scheduleId);

    // 특정 scheduleId를 가지는 scheduleItem 들 중에서
    // index 값을 구하고자하는 scheduleItem 보다 itemOrder 가 작은 데이터의 갯수를 COUNT
    // 예) 1번아이템의 itemOrder 는 1.25, 2번은 3.0, 3번은 3.5 일 경우
    //     - 2번 보다 작은 itemOrder 은 1개이므로 2번의 index 1을 반환
    @Query("SELECT COUNT(si) " +
            "FROM ScheduleItem si " +
            "WHERE si.marker.schedule.scheduleId = :scheduleId " +
            "AND si.itemOrder < (SELECT s.itemOrder FROM ScheduleItem s WHERE s.scheduleItemId = :scheduleItemId)")
    int findIndexByScheduleItemId(@Param("scheduleItemId") Long scheduleItemId, @Param("scheduleId") Long scheduleId);

    // scheduleId 로 조회 후
    // 두개의 itemOrder 사이의 값을 가지는 데이터가 있는지 확인
    @Query("SELECT CASE WHEN COUNT(si) > 0 THEN true ELSE false END " +
            "FROM ScheduleItem si " +
            "WHERE si.marker.schedule.scheduleId = :scheduleId " +
            "AND si.itemOrder > :startItemOrder " +
            "AND si.itemOrder < :endItemOrder")
    Boolean existsBetweenItemOrders(
            @Param("scheduleId") Long scheduleId,
            @Param("startItemOrder") Double startItemOrder,
            @Param("endItemOrder") Double endItemOrder
    );
}

