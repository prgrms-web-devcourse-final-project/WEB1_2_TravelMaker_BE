package edu.example.wayfarer.handler;

import edu.example.wayfarer.dto.chatMessage.ChatMessageRequestDTO;
import edu.example.wayfarer.service.ChatMessageService;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.example.wayfarer.handler.ChatHandler.CHAT_CACHE_PREFIX;

@Component
@RequiredArgsConstructor
@Log4j2
public class RedisHandler {

    @Qualifier("jsonRedisTemplate")
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ChatMessageService chatMessageService;

    // 12시간에 한번씩 DB에 저장 (배포용)
    //@Scheduled(fixedRate = 43200000)

    // 60초에 한번씩 DB에 저장 (테스트용)
    //@Scheduled(fixedRate = 60000)

    // 12시간에 한번씩 DB에 저장 (배포용)
    @Scheduled(fixedRate = 43200000)
    public void migrateMessagesTODB() {
        log.info("Starting Redis to DB migration...");

        //Redis 에서 ChatMessage: 로 시작하는 모든 key 값 검색
        Set<String> keys = jsonRedisTemplate.keys(CHAT_CACHE_PREFIX+"*");
        log.debug("Found keys: {}", keys);
        //key 값이 존재하지 않으면, DB에 저장할 데이터가 없으므로 메서드 종료
        if (keys == null || keys.isEmpty()) {
            log.info("No keys found in Redis.");
            log.info("Migration complete.");
            return;
        }

        // 각 key 값 마다 List 형태로 된 value 조회
        for (String key : keys) {
            List<Object> messageList = jsonRedisTemplate.opsForList().range(key, 0, -1);
            log.debug("messageList: {}", messageList);

            //List 내부에 있는 각각의 메시지는 Map 형태로 존재
            for (Object message : messageList) {
                if (!(message instanceof Map<?,?> messageData)) {
                    log.error("Unexpected message data format in key: {}", key);
                    throw new RedisException("Unexpected message data format in key");
                }

                //메시지에서 정보 추출
                Map<?, ?> data = (Map<?, ?>) messageData.get("data");

                String roomId = key.split(":")[1];
                String email = (String) data.get("sender");
                String content = (String) data.get("message");
                String timestamp = (String) data.get("timestamp");

                // ChatMessageRequestDTO 객체 생성
                ChatMessageRequestDTO chatMessageRequestDTO = new ChatMessageRequestDTO(roomId, email, content, timestamp);

                // Redis 데이터가 이미 DB에 저장된 메시지인지 확인
                if (!chatMessageService.isMessageExistsInDB(chatMessageRequestDTO)) {
                    // 필터링된 메시지들만 DB에 저장
                    chatMessageService.createChatMessage(chatMessageRequestDTO);
                    log.info("Message saved to DB: roomId={}, email={}, content={}", roomId, email, content);
                } else {
                    //중복된 DB는 저장하지 않고 로그에 표시
                    log.info("Message already exists in DB: roomId={}, email={}, content={}", roomId, email, content);
                }
            }
        }
        log.info("Migration complete.");
    }
}
