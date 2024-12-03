package edu.example.wayfarer.service;

import edu.example.wayfarer.converter.ChatMessageConverter;
import edu.example.wayfarer.dto.chatMessage.ChatMessageListDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageResponseDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageUpdateDTO;
import edu.example.wayfarer.entity.ChatMessage;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.exception.ChatMessageException;
import edu.example.wayfarer.exception.MemberException;
import edu.example.wayfarer.exception.RoomException;
import edu.example.wayfarer.repository.ChatMessageRepository;
import edu.example.wayfarer.repository.MemberRepository;
import edu.example.wayfarer.repository.MemberRoomRepository;
import edu.example.wayfarer.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MemberRoomRepository memberRoomRepository;

    @Override
    public ChatMessageResponseDTO createChatMessage(ChatMessageRequestDTO chatMessageRequestDTO) {
        Room room = roomRepository.findById(chatMessageRequestDTO.roomId())
                .orElseThrow(RoomException.NOT_FOUND::get);
        Member member = memberRepository.findByEmail(chatMessageRequestDTO.email())
                .orElseThrow(MemberException.NOT_FOUND::get);

        // 해당 사용자가 방의 참여자인지 확인
        verifyMemberInRoom(chatMessageRequestDTO.email(), chatMessageRequestDTO.roomId());

        ChatMessage chatMessage = ChatMessageConverter.toChatMessage(room, member, chatMessageRequestDTO.content());
        chatMessageRepository.save(chatMessage);
        return ChatMessageConverter.toChatMessageResponseDTO(chatMessage);
    }

    @Override
    public ChatMessageResponseDTO updateChatMessage(ChatMessageUpdateDTO chatMessageUpdateDTO) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageUpdateDTO.chatMessageId())
                .orElseThrow(ChatMessageException.MESSAGE_NOT_FOUND::get);

        verifyWriter(chatMessage, chatMessageUpdateDTO.email());
        chatMessage.changeContent(chatMessageUpdateDTO.content());
        chatMessageRepository.save(chatMessage);
        return ChatMessageConverter.toChatMessageResponseDTO(chatMessage);
    }

    @Override
    public void deleteChatMessage(Long chatMessageId, String email) {
        ChatMessage chatMessage = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(ChatMessageException.MESSAGE_NOT_FOUND::get);

        verifyWriter(chatMessage, email);
        chatMessageRepository.delete(chatMessage);
    }

    @Override
    public List<ChatMessageListDTO> getChatMessageListDTOByRoomId(String roomId) {
        return chatMessageRepository.findChatMessageListDTOByRoomId(roomId);
    }

    private void verifyMemberInRoom(String email, String roomId){
        memberRoomRepository.findByMemberEmailAndRoomRoomId(email, roomId)
                .orElseThrow(MemberException.NOT_FOUND::get);
    }

    private void verifyWriter(ChatMessage chatMessage, String email){
        if(!chatMessage.getMember().getEmail().equals(email)){
            throw(ChatMessageException.UNAUTHORIZED.get());
        }
    }
}
