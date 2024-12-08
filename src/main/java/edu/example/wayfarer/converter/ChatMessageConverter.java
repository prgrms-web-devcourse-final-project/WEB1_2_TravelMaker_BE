package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.chatMessage.ChatMessageResponseDTO;
import edu.example.wayfarer.entity.ChatMessage;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Room;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ChatMessageConverter {

    public static ChatMessage toChatMessage(Room room, Member member,String content, String timestamp) {
        LocalDateTime time = stringTOLocalDateTime(timestamp);

        return ChatMessage.builder()
                .room(room)
                .member(member)
                .content(content)
                .createdAt(time)
                .build();
    }

    public static ChatMessageResponseDTO toChatMessageResponseDTO(ChatMessage chatMessage) {
        return new ChatMessageResponseDTO(
                chatMessage.getRoom().getRoomId(),
                chatMessage.getMember().getEmail(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );

    }

    public static LocalDateTime stringTOLocalDateTime(String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss.SSS z yyyy", Locale.ENGLISH);

        // KST를 Asia/Seoul로 바꿔서 파싱
        timestamp = timestamp.replace("KST", "Asia/Seoul");

        // ZonedDateTime을 사용하여 시간대 정보를 포함한 파싱
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp, formatter);

        return zonedDateTime.toLocalDateTime();

    }
}
