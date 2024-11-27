package edu.example.wayfarer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import edu.example.wayfarer.service.ScheduleItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Log4j2
public class ScheduleItemController {
    private final SimpMessagingTemplate template;
    private final ScheduleItemService scheduleItemService;
    private final ObjectMapper objectMapper;

    @MessageMapping("room/{roomId}/schedule")
    public void handleSchedule(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> schedulePayload
    ) {
        String action = (String) schedulePayload.get("action");

        switch (action) {
            case "LIST_SCHEDULES":
                Long scheduleId = ((Number) ((Map<String, Object>) schedulePayload.get("data")).get("scheduleId")).longValue();
                List<ScheduleItemResponseDTO> scheduleItems = scheduleItemService.getListBySchedule(scheduleId);

                WebSocketMessageConverter<List<ScheduleItemResponseDTO>> listConverter = new WebSocketMessageConverter<>();

                WebSocketMessageConverter.WebsocketMessage<List<ScheduleItemResponseDTO>> listSchedulesMessage=
                listConverter.createMessage("LIST_SCHEDULES", scheduleItems);

                template.convertAndSend("/topic/room/" + roomId + "/schedule", listSchedulesMessage);

                break;


            case "UPDATE_SCHEDULE":
                //schedulePayload로 받아온 값으로 ScheduleItemUpdateDTO 생성
                Integer intScheduleItemId = (Integer) ((Map<String, Object>) schedulePayload.get("data")).get("scheduleItemId");
                Long scheduleItemId = intScheduleItemId.longValue();
                String name = ((Map<String, Object>) schedulePayload.get("data")).get("name").toString();
                String content = ((Map<String, Object>) schedulePayload.get("data")).get("content").toString();
                Long previousItemId = ((Number) ((Map<String, Object>) schedulePayload.get("data")).get("previousItemId")).longValue();
                Long nextItemId = ((Number) ((Map<String, Object>) schedulePayload.get("data")).get("nextItemId")).longValue();


                ScheduleItemUpdateDTO scheduleItemUpdateDTO = new ScheduleItemUpdateDTO(scheduleItemId,name,content,previousItemId,nextItemId);

                // ScheduleItemUpdateDTO로 scheduleItemService.update() 실행
                ScheduleItemResponseDTO updatedScheduleItem = scheduleItemService.update(scheduleItemUpdateDTO);

                // WebSocketMessageConverter를 사용해 메시지 객체 생성
                WebSocketMessageConverter<ScheduleItemResponseDTO> updateConverter = new WebSocketMessageConverter<>();
                WebSocketMessageConverter.WebsocketMessage<ScheduleItemResponseDTO> updatedScheduleItemMessage =
                        updateConverter.createMessage("UPDATED_SCHEDULE", updatedScheduleItem);

                //생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
                template.convertAndSend("/topic/room/" + roomId + "/schedule", updatedScheduleItemMessage);

                break;

            case "DELETE_SCHEDULE":
                //1. 스케쥴 아이템 삭제
                Integer intDeleteScheduleItemId = (Integer) ((Map<String, Object>) schedulePayload.get("data")).get("scheduleItemId");
                Long deleteScheduleItemId = intDeleteScheduleItemId.longValue();
                scheduleItemService.delete(deleteScheduleItemId);

                //2. 스케줄 아이템 삭제 메시지 전송, 마커 업데이트 메시지 전송
                Map<String, Object> deletedScheduleItemMessage = new LinkedHashMap<>();
                deletedScheduleItemMessage.put("action", "DELETED_SCHEDULE");
                deletedScheduleItemMessage.put("data", Map.of(
                        "message", "일정이 삭제되었습니다."
                ));

                //3. 생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
                template.convertAndSend("/topic/room/" + roomId + "/schedule", deletedScheduleItemMessage);

                break;


            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

}
