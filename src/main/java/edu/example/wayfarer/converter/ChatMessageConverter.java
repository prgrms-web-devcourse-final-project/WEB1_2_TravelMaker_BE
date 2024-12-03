package edu.example.wayfarer.converter;

import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageResponseDTO;
import edu.example.wayfarer.entity.ChatMessage;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Room;

public class ChatMessageConverter {

    public static ChatMessage toChatMessage(Room room, Member member,String content) {
        return ChatMessage.builder()
                .room(room)
                .member(member)
                .content(content)
                .build();
    }

    public static ChatMessageResponseDTO toChatMessageResponseDTO(ChatMessage chatMessage) {
        return new ChatMessageResponseDTO(
                chatMessage.getRoom().getRoomId(),
                chatMessage.getMember().getEmail(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt(),
                chatMessage.getUpdatedAt()
        );

    }
}
