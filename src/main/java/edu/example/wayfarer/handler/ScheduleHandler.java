package edu.example.wayfarer.handler;

import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.schedule.ScheduleListDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import edu.example.wayfarer.service.ScheduleItemService;
import edu.example.wayfarer.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class ScheduleHandler {
    private final SimpMessagingTemplate template;
    private final ScheduleItemService scheduleItemService;
    private final ScheduleService scheduleService;


    public void handleSchedule (String roomId, Map<String, Object> schedulePayload) {
        // 클라이언트가 보낸 Payload를 action과 data로 분리
        String action = (String) schedulePayload.get("action");
        Map<String, Object> data = (Map<String, Object>) schedulePayload.get("data");
        log.debug("action: " + action);

        switch (action) {
            case "LIST_SCHEDULES" -> listSchedules(roomId);
            case "LIST_SCHEDULEITEMS" -> listScheduleItems(roomId, data);
            case "UPDATE_SCHEDULEITEM" -> updateScheduleItem(roomId, data);
            case "DELETE_SCHEDULEITEM" -> deleteScheduleItem(roomId, data);
            default -> throw new WebSocketTaskException(WebSocketException.INVALID_ACTION);
        }
    }

    private void listSchedules(String roomId) {

        // 전달받은 roomId로 ScheduleList 불러오기
        List<ScheduleListDTO> schedules = scheduleService.getScheduleListByRoomId(roomId);
        // WebSocketMessageConverter로 List Schedule message 생성
        WebSocketMessageConverter<List<ScheduleListDTO>> scheduleListConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<List<ScheduleListDTO>> listSchedulesMessage =
                scheduleListConverter.createMessage("LIST_SCHEDULES", schedules);

        // 생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/schedule", listSchedulesMessage);
    }

    private void listScheduleItems(String roomId, Map<String, Object> data) {

        // 클라이언트가 송신한 메시지의 data에서 scheduleId값 추출 -> list 불러오기
        Long scheduleId = ((Number) data.get("scheduleId")).longValue();
        List<ScheduleItemResponseDTO> scheduleItems = scheduleItemService.getListBySchedule(scheduleId);

        // WebSocketMessageConverter로 List Schedule message 생성
        WebSocketMessageConverter<List<ScheduleItemResponseDTO>> listConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<List<ScheduleItemResponseDTO>> listScheduleItemsMessage =
                listConverter.createMessage("LIST_SCHEDULEITEMS", scheduleItems);

        // 생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/schedule", listScheduleItemsMessage);

    }

    private void updateScheduleItem(String roomId, Map<String, Object> data) {

        // 클라이언트가 송신한 메시지의 data로 ScheduleItemUpdateDTO 생성
        Long scheduleItemId = ((Number) data.get("scheduleItemId")).longValue();
        String name = data.get("name").toString();
        String content = data.get("content").toString();
        Long previousItemId = Optional.ofNullable((Number) data.get("previousItemId"))
                .map(Number::longValue)
                .orElse(null);
        Long nextItemId = Optional.ofNullable((Number) data.get("nextItemId"))
                .map(Number::longValue)
                .orElse(null);

        ScheduleItemUpdateDTO scheduleItemUpdateDTO = new ScheduleItemUpdateDTO(scheduleItemId,name,content,previousItemId,nextItemId);

        // ScheduleItemUpdateDTO로 scheduleItemService.update() 실행
        ScheduleItemResponseDTO updatedScheduleItem = scheduleItemService.update(scheduleItemUpdateDTO);

        // WebSocketMessageConverter로 Update ScheduleItem message 생성
        WebSocketMessageConverter<ScheduleItemResponseDTO> updateConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<ScheduleItemResponseDTO> updatedScheduleItemMessage =
                updateConverter.createMessage("UPDATED_SCHEDULEITEM", updatedScheduleItem);

        // 생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/schedule", updatedScheduleItemMessage);

    }

    private void deleteScheduleItem(String roomId, Map<String, Object> data) {

        // 클라이언트가 송신한 메시지의 data로 ScheduleItemId 추출 -> delete 실행
        Long deleteScheduleItemId = ((Number) data.get("scheduleItemId")).longValue();
        scheduleItemService.delete(deleteScheduleItemId);

        // Delete 메시지 생성
        Map<String, Object> deletedScheduleItemMessage = new LinkedHashMap<>();
        deletedScheduleItemMessage.put("action", "DELETED_SCHEDULEITEM");
        deletedScheduleItemMessage.put("data", Map.of(
                "message", "일정이 삭제되었습니다."
        ));

        // 생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/schedule", deletedScheduleItemMessage);

    }
}
