package edu.example.wayfarer.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.converter.ChatMessageConverter;
import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.dto.chatMessage.ChatMessageResponseDTO;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import edu.example.wayfarer.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class ChatHandler {
    private final SimpMessagingTemplate template;
    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    public static final String CHAT_CACHE_PREFIX = "ChatMessage:";


    @Autowired
    @Qualifier("jsonRedisTemplate")
    private RedisTemplate<String, Object> jsonRedisTemplate;

    // 타임스탬프 포맷 정의 (밀리세컨드까지 포함)
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss.SSS z yyyy", Locale.ENGLISH);


    public void handleChat(String roomId, String email, Map<String, Object> messagePayload) {
        //클라이언트가 보낸 Payload를 action과 data로 분리
        String action = (String) messagePayload.get("action");
        Map<String, Object> data = (Map<String, Object>) messagePayload.get("data");

        log.debug("action: " + action);

        switch (action) {
            case "ENTER_ROOM" -> sendWelcomeMessage(roomId, email);
            case "SEND_MESSAGE" -> broadcastMessage(roomId, email, data);
            case "LIST_MESSAGES" -> listMessages(roomId);
            default -> throw new WebSocketTaskException(WebSocketException.INVALID_ACTION);
        }
    }



    private void sendWelcomeMessage(String roomId, String email) {
        Map<String, Object> welcomeMessage = new LinkedHashMap<>();
        welcomeMessage.put("action", "WELCOME_MESSAGE");
        welcomeMessage.put("data", Map.of(
                        "sender", "System",
                        "message", email + " 님이 입장하셨습니다.",
                        "timestamp", timestampFormat.format(new Date())
                )
        );

        log.debug("WELCOME MESSAGE: " + welcomeMessage);

        template.convertAndSend("/topic/room/" + roomId, welcomeMessage);
    }

    private void broadcastMessage(String roomId, String email, Map<String, Object> data) {
        String memberJson = (String) jsonRedisTemplate.opsForHash().get("Member:" + roomId, email);
        String nickname=null;
        String profileImage=null;
        if (memberJson != null) {
            try {
                // JSON 문자열을 JsonNode로 변환
                JsonNode jsonNode = objectMapper.readTree(memberJson);
                // nickname과 profileImage 추출
                nickname = jsonNode.get("nickname").asText();
                profileImage = jsonNode.get("profileImage").asText();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        String message = (String) data.get("message");
        if (message == null) {
            throw new WebSocketTaskException(WebSocketException.INVALID_MESSAGE_FORMAT);
        }

        Map<String, Object> broadcastMessage = new LinkedHashMap<>();
        broadcastMessage.put("action", "BROADCAST_MESSAGE");
        broadcastMessage.put("data", Map.of(
                "sender", email,
                "message", message,
                "timestamp", timestampFormat.format(new Date()),
                "nickname", nickname,
                "profileImage", profileImage
                )
        );

        //Redis에 chatMesssage 캐시 저장
        jsonRedisTemplate.opsForList().rightPush(CHAT_CACHE_PREFIX + roomId, broadcastMessage);
        jsonRedisTemplate.expire(CHAT_CACHE_PREFIX + roomId, 1, TimeUnit.DAYS);

        log.debug("BROADCAST_MESSAGE: " + broadcastMessage);

        template.convertAndSend("/topic/room/" + roomId, broadcastMessage);
    }


        private void listMessages(String roomId) {

            //레디스에서 메시지 읽기
            List<ChatMessageResponseDTO> redisMessages = readMessagesFromRedis(roomId);
            log.debug("redisMessages: {}", redisMessages);
            //레디스에서 읽은 메시지가 있으면 가장 첫 번째 메시지를 기준으로 timestamp 추출
            LocalDateTime latestRedisTimestamp = null;
            if (!redisMessages.isEmpty()) {
                ChatMessageResponseDTO firstRedisMessage = redisMessages.get(0); // 레디스에 저장된 가장 오래된 메시지
                latestRedisTimestamp = firstRedisMessage.createdAt();
            }

            //DB에서 레디스에 저장된 메시지보다 이전 시간대의 메시지 읽어오기
            List<ChatMessageResponseDTO> dbMessages = new ArrayList<>();
            if (latestRedisTimestamp != null) {
                // 레디스 메시지보다 이전에 생성된 메시지들을 DB에서 읽어옴
                dbMessages = getChatMessagesBeforeTimestamp(roomId, latestRedisTimestamp);
            }
            log.debug("DB Messages: {}", dbMessages);

            //레디스와 DB에서 읽은 메시지를 합치기 (중복 제거)
            List<ChatMessageResponseDTO> allMessages = mergeMessages(redisMessages, dbMessages);

            //WebSocketMessageConverter로 List message 생성
            WebSocketMessageConverter<List<ChatMessageResponseDTO>> listConverter = new WebSocketMessageConverter<>();
            WebSocketMessageConverter.WebsocketMessage<List<ChatMessageResponseDTO>> listMessage =
                    listConverter.createMessage("LIST_MESSAGES", allMessages);
            log.debug("LIST MESSAGES: " + listMessage);

            template.convertAndSend("/topic/room/" + roomId, listMessage);


        }

        private List<ChatMessageResponseDTO> readMessagesFromRedis(String roomId) {
            // Redis에서 메시지 가져오기
            List <Object> redisMessages = jsonRedisTemplate.opsForList().range(CHAT_CACHE_PREFIX+roomId, 0, -1);
            // 메시지를 DTO로 변환
            if (redisMessages == null || redisMessages.isEmpty()) {
                return new ArrayList<>();
            }

            return redisMessages.stream()
                    .filter(message -> message instanceof Map<?, ?>) // 메시지가 Map 형태인지 확인
                    .map(message -> {
                        @SuppressWarnings("unchecked") // 타입 경고 억제
                        Map<String, Object> messageMap = (Map<String, Object>) message;
                        return convertToChatMessageResponseDTO(roomId, messageMap);
                    })
                    .toList();
        }

        private ChatMessageResponseDTO convertToChatMessageResponseDTO(String roomId, Map<String, Object> messageMap) {
            // 메시지 맵에서 필요한 데이터 추출

            String email = (String) ((Map<?, ?>) messageMap.get("data")).get("sender");
            String content = (String) ((Map<?, ?>) messageMap.get("data")).get("message");
            String stringCreatedAt = (String) ((Map<?, ?>) messageMap.get("data")).get("timestamp");
            LocalDateTime createdAt = ChatMessageConverter.stringTOLocalDateTime(stringCreatedAt);

            // ChatMessageResponseDTO 객체 생성 및 반환
            return new ChatMessageResponseDTO(roomId, email, content, createdAt);
        }

        // DB에서 레디스 메시지 시간대 이전의 메시지 불러오기
        private List<ChatMessageResponseDTO> getChatMessagesBeforeTimestamp(String roomId, LocalDateTime createdAt) {
            return chatMessageService.getChatMessagesBeforeTimestamp(roomId, createdAt);
        }

        private List<ChatMessageResponseDTO> mergeMessages(List<ChatMessageResponseDTO> redisMessages, List<ChatMessageResponseDTO> dbMessages) {
            // 중복 제거 및 합치기
            Set<ChatMessageResponseDTO> uniqueMessages = new HashSet<>();
            uniqueMessages.addAll(redisMessages);
            uniqueMessages.addAll(dbMessages);

            return uniqueMessages.stream()
                    .sorted(Comparator.comparing(ChatMessageResponseDTO::createdAt))
                    .collect(Collectors.toList());
        }

    }