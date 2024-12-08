package edu.example.wayfarer.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.member.MemberResponseDTO;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class MemberHandler {
    @Autowired
    @Qualifier("jsonRedisTemplate")
    private RedisTemplate<String, Object> jsonRedisTemplate;
    private final SimpMessagingTemplate template;
    private final ObjectMapper objectMapper;

    public void handleMember(String roomId, Map<String, Object> memberPayload) {
        //클라이언트가 보낸 Payload를 action과 data로 분리
        String action = (String) memberPayload.get("action");
        Map<String, Object> data = (Map<String, Object>) memberPayload.get("data");

        log.debug("action: " + action);

        if (action.equals("LIST_MEMBERS")) {
            // 특정 키에 저장된 모든 필드와 값을 조회
            Map<Object, Object> roomMembers = jsonRedisTemplate.opsForHash().entries("Member:" + roomId);
            List<MemberResponseDTO> memberList = new ArrayList<>();

            for (Map.Entry<Object, Object> entry : roomMembers.entrySet()) {
                String email = (String) entry.getKey();
                String memberInfo = (String) entry.getValue();
                try {
                    // JSON 문자열을 JsonNode로 변환
                    JsonNode jsonNode = objectMapper.readTree(memberInfo);

                    // 닉네임과 프로필 이미지 가져오기
                    String nickname = jsonNode.get("nickname").asText();
                    String profileImage = jsonNode.get("profileImage").asText();

                    MemberResponseDTO memberResponseDTO = new MemberResponseDTO(email, nickname, profileImage);
                    memberList.add(memberResponseDTO);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            WebSocketMessageConverter<List<MemberResponseDTO>> listConverter = new WebSocketMessageConverter<>();
            WebSocketMessageConverter.WebsocketMessage<List<MemberResponseDTO>> listMembersMessage =
                    listConverter.createMessage("LIST_MEMBERS", memberList);
            log.debug("listMembersMessage: " + listMembersMessage);

            template.convertAndSend("/topic/room/" + roomId + "/member", listMembersMessage);
        } else {
            throw new WebSocketTaskException(WebSocketException.INVALID_ACTION);
        }
    }
}
