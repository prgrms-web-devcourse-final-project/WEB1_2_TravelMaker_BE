package edu.example.wayfarer.service;

import edu.example.wayfarer.dto.chatMessage.ChatMessageListDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageUpdateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ChatMessageTests {

    @Autowired
    private ChatMessageService chatMessageService;

    @Test
    @Transactional
    @Commit
    public void testCreateChatMessage() {
        ChatMessageRequestDTO chatMessageRequestDTO = new ChatMessageRequestDTO(
            "mP5LcSAt",
                "aa@aa.com",
                "이곳에 나홀로"
        );
        chatMessageService.createChatMessage(chatMessageRequestDTO);
    }

    @Test
    @Transactional
    @Commit
    public void testUpdateChatMessage() {
        ChatMessageUpdateDTO chatMessageUpdateDTO = new ChatMessageUpdateDTO(
                2L,
                "bb@bb.com",
                "아 안녕 ㅋㅋ"
        );
        chatMessageService.updateChatMessage(chatMessageUpdateDTO);
    }

    @Test
    @Transactional
    @Commit
    public void testDeleteChatMessage() {
        Long chatMessageId = 1L;
        String email = "bb@bb.com";
        chatMessageService.deleteChatMessage(chatMessageId, email);
    }

    @Test
    public void testMessageListByRoomId(){
        String roomId = "728t5EIw";
        List<ChatMessageListDTO> messageList = chatMessageService.getChatMessageListDTOByRoomId(roomId);
        System.out.println(messageList);
    }
}
