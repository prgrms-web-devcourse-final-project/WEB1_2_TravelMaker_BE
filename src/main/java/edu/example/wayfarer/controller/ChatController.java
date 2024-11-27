package edu.example.wayfarer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate template;

    @MessageMapping("/room/{roomId}")
    public void handleChatMessage(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> messagePayload
    ) {
        String action = (String) messagePayload.get("action");
        System.out.println("action: " + action);

        if("ENTER_ROOM".equals(action)) {
            String email = (String) ((Map<String, Object>) messagePayload.get("data")).get("sender");

            Map<String, Object> welcomeMessage = new LinkedHashMap<>();
            welcomeMessage.put("action", "WELCOME_MESSAGE");
            welcomeMessage.put("data", Map.of(
                    "sender", "System",
                    "message", email+" 님이 입장하셨습니다.",
                    "timestamp", new Date().toString()
            ));

            System.out.println(welcomeMessage);
            template.convertAndSend("/topic/room/" + roomId, welcomeMessage);
        } else if ("SEND_MESSAGE".equals(action)) {
            template.convertAndSend("/topic/room/" + roomId, messagePayload);
        }
    }
}

//enter room 액션에서 sender값이 null, 유효하지 않은 경우 예외처리