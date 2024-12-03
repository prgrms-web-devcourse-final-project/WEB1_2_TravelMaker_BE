package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.chatMessage.ChatMessageListDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageResponseDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageUpdateDTO;
import edu.example.wayfarer.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {

    ChatMessageResponseDTO createChatMessage(ChatMessageRequestDTO chatMessageRequestDTO);

    ChatMessageResponseDTO updateChatMessage(ChatMessageUpdateDTO chatMessageUpdateDTO);

    void deleteChatMessage(Long chatMessageId, String email);

    List<ChatMessageListDTO> getChatMessageListDTOByRoomId(String roomId);

}
