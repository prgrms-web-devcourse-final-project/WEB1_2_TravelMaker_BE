package edu.example.wayfarer.repository;

import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.enums.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

public interface MemberRoomRepository extends JpaRepository<MemberRoom, Long> {
    Optional<MemberRoom> findByMemberEmailAndRoomRoomId(String email, String roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM MemberRoom mr WHERE mr.member.email = :email AND mr.room.roomId = :roomId")
    void deleteByEmailAndRoomId(@Param("email") String email, @Param("roomId") String roomId);

    // 특정 Room에 해당 Color가 이미 사용중인지 확인
    boolean existsByRoomRoomIdAndColor(String roomId, Color color);

    /* Room에 속한 모든 MemberRoom 조회 */
    List<MemberRoom> findAllByRoomRoomId(String roomId);
    /* 한 사용자가 참여하고 있는 모든방 */
    List<MemberRoom> findAllByMemberEmail(String email);
}
