package edu.example.wayfarer.repository;

import edu.example.wayfarer.dto.chatMessage.ChatMessageListDTO;
import edu.example.wayfarer.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT new edu.example.wayfarer.dto.chatMessage.ChatMessageListDTO(cm.room.roomId, cm.content) " +
            "FROM ChatMessage cm " +
            "WHERE cm.room.roomId = :roomId")
    List<ChatMessageListDTO> findChatMessageListDTOByRoomId(String roomId);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.room.roomId = :roomId")
    void deleteByRoomId(@Param("roomId") String roomId);
}
