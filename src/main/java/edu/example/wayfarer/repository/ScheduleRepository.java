package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.enums.Days;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByRoom_RoomId(String roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Schedule s WHERE s.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") String roomId);

    List<Schedule> findByRoomAndDate(Room room, Days day);
}
