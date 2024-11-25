package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.entity.enums.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRoomRepository extends JpaRepository<MemberRoom, Long> {
    Optional<MemberRoom> findByMember_EmailAndRoom_RoomId(String email, String roomId);

    @Query("select mb from MemberRoom mb where mb.room.roomId = :roomId")
    public MemberRoom findByRoomId(@Param("roomId") String roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MemberRoom mr WHERE mr.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") String roomId);

    // 특정 Room에 해당 Color가 이미 사용중인지 확인
    boolean existsByRoom_RoomIdAndColor(String roomId, Color color);

    void findByRoom(Room room);

    List<MemberRoom> findAllByRoom_RoomId(String roomId);   // Room에 속한 모든 MemberRoom 조회
    List<MemberRoom> findAllByMember_Email(String email);   // 한 사용자가 참여하고 있는 모든방
}
