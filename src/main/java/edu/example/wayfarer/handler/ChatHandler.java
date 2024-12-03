package edu.example.wayfarer.handler;

import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import edu.example.wayfarer.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class ChatHandler {
    private final SimpMessagingTemplate template;
    private final ChatMessageService chatMessageService;

    public void handleChat(String roomId, String email, Map<String, Object> messagePayload) {
        //클라이언트가 보낸 Payload를 action과 data로 분리
        String action = (String) messagePayload.get("action");
        Map<String, Object> data = (Map<String, Object>) messagePayload.get("data");

        log.debug("action: " + action);

        switch (action) {
            case "ENTER_ROOM" -> sendWelcomeMessage(roomId, email);
            case "SEND_MESSAGE" -> broadcastMessage(roomId, email, data);
            default -> throw new WebSocketTaskException(WebSocketException.INVALID_ACTION);
        }
    }

    private void sendWelcomeMessage(String roomId, String email) {
        Map<String, Object> welcomeMessage = new LinkedHashMap<>();
        welcomeMessage.put("action", "WELCOME_MESSAGE");
        welcomeMessage.put("data", Map.of(
                "sender", "System",
                "message", email + " 님이 입장하셨습니다.",
                "timestamp", new Date().toString())
        );

        log.debug("WELCOME MESSAGE: " + welcomeMessage);

        template.convertAndSend("/topic/room/" + roomId, welcomeMessage);
    }

    private void broadcastMessage(String roomId, String email, Map<String, Object> data) {
        String message = (String) data.get("message");
        if (message == null) {
            throw new WebSocketTaskException(WebSocketException.INVALID_MESSAGE_FORMAT);
        }

        Map<String, Object> broadcastMessage = new LinkedHashMap<>();
        broadcastMessage.put("action", "BROADCAST_MESSAGE");
        broadcastMessage.put("data", Map.of(
                "sender", email,
                "message", message,
                "timestamp", new Date().toString())
        );

        //chatMessage DB에 저장
        ChatMessageRequestDTO chatMessageRequestDTO = new ChatMessageRequestDTO (roomId, email, message);
        chatMessageService.createChatMessage(chatMessageRequestDTO);

        log.debug("BROADCAST_MESSAGE: " + broadcastMessage);

        template.convertAndSend("/topic/room/" + roomId, broadcastMessage);
    }

}
