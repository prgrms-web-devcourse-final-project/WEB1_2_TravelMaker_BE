package edu.example.wayfarer.controller;

import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import edu.example.wayfarer.handler.ChatHandler;
import edu.example.wayfarer.handler.MarkerHandler;
import edu.example.wayfarer.handler.MemberHandler;
import edu.example.wayfarer.handler.ScheduleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class WebSocketController {
    private final ChatHandler chatHandler;
    private final MarkerHandler markerHandler;
    private final ScheduleHandler scheduleHandler;
    private final MemberHandler memberHandler;

    // WebSocket 연결 이벤트 처리
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String email = getEmail(stompHeaderAccessor);

        log.info("WebSocket Connected Email: " + email);
    }

    // 채팅 메시지 처리
    @MessageMapping("/room/{roomId}")
    public void handleChatMessage(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> messagePayload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String email = getEmail(headerAccessor);
        chatHandler.handleChat(roomId, email, messagePayload);
    }

    // 지도 마커 처리
    @MessageMapping("room/{roomId}/map")
    public void handleMarker(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> markerPayload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String email = getEmail(headerAccessor);
        markerHandler.handleMarker(roomId, email, markerPayload);

    }

    // 일정 관리 처리
    @MessageMapping("room/{roomId}/schedule")
    public void handleSchedule(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> schedulePayload
    ) {
        scheduleHandler.handleSchedule(roomId, schedulePayload);

    }

    // 멤버 관련 처리
    @MessageMapping("room/{roomId}/member")
    public void handleMember(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> memberPayload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        memberHandler.handleMember(roomId, memberPayload);
    }

    private String getEmail(SimpMessageHeaderAccessor headerAccessor) {
        String email = (String) headerAccessor.getSessionAttributes().get("email");

        if (email == null) {
            throw new WebSocketTaskException(WebSocketException.INVALID_EMAIL);
        }
        return email;
    }
}
