package edu.example.wayfarer.handler;

import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.marker.MarkerUpdateDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import edu.example.wayfarer.service.MarkerService;
import edu.example.wayfarer.service.ScheduleItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
public class MarkerHandler {
    private final SimpMessagingTemplate template;
    private final MarkerService markerService;
    private final ScheduleItemService scheduleItemService;

    /*
    클라이언트에서 송신한 메시지의 "action" 항목으로 기능 구분
    MarkerService의 메서드 리턴값인 MarkerResponseDTO의 내용으로
    클라이언트에 브로드캐스팅 할 JSON 메시지 구현
    */

    public void handleMarker(String roomId, String email, Map<String, Object> markerPayload) {
        //클라이언트가 보낸 Payload를 action과 data로 분리
        String action = (String) markerPayload.get("action");
        Map<String, Object> data = (Map<String, Object>) markerPayload.get("data");
        log.debug("action: " + action);

        switch (action) {
            case "ADD_MARKER" -> addMarker(roomId, email, data);
            case "LIST_MARKERS" -> listMarkers(roomId, data);
            case "UPDATE_MARKER" -> updateMarker(roomId, data);
            case "DELETE_MARKER" -> deleteMarker(roomId, data);
            default -> throw new WebSocketTaskException(WebSocketException.INVALID_ACTION);
        }
    }


    private void addMarker(String roomId, String email, Map<String, Object> data) {

        //클라이언트가 송신한 메시지의 data에서 scheduleId, lat, lng 값을 추출
        Long scheduleId = ((Number) data.get("scheduleId")).longValue();
        Double lat = ((Number) data.get("lat")).doubleValue();
        Double lng = ((Number) data.get("lng")).doubleValue();

        //추출 한 값으로 MarkerRequestDTO 생성
        MarkerRequestDTO markerRequestDTO = new MarkerRequestDTO(email,scheduleId,lat,lng);
        //MarkerRequestDTO로 markService.create() 실행
        MarkerResponseDTO addedMarker = markerService.create(markerRequestDTO);
        //WebSocketMessageConverter로 add message 생성
        WebSocketMessageConverter<MarkerResponseDTO> addConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<MarkerResponseDTO> addedMarkerMessage =
                addConverter.createMessage("ADDED_MARKER", addedMarker);

        log.debug("AddedMarkerMessage: " + addedMarkerMessage);
        //생성한 메시지를 "topic/schedule/{roomId}/map" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/map", addedMarkerMessage);

    }

    private void listMarkers(String roomId, Map<String, Object> data) {

        //클라이언트가 송신한 메시지의 data에서 scheduleId 값을 추출
        Long listScheduleId = ((Number) data.get("scheduleId")).longValue();

        List<MarkerResponseDTO> markers = markerService.getListBySchedule(listScheduleId);

        //WebSocketMessageConverter로 List message 생성
        WebSocketMessageConverter<List<MarkerResponseDTO>> listConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<List<MarkerResponseDTO>> listMarkersMessage =
                listConverter.createMessage("LIST_MARKERS", markers);

        log.debug("ListMarkerMessage: " + listMarkersMessage);
        //생성한 메시지를 "topic/schedule/{roomId}/map" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/map", listMarkersMessage);
    }

    /*
    전달 받은 confirm 값이 true 이면
    markerService.update() 과정에서 등록된 scheduleItem 의 값을 받아와
    ADDED_SCHEDULE 액션의 메시지를 송신한다
     */
    private void updateMarker(String roomId, Map<String, Object> data) {

        //클라이언트가 송신한 메시지의 data에서 markerId, confirm 값을 추출
        Long markerId = ((Number) data.get("markerId")).longValue();
        Boolean confirm = (Boolean) data.get("confirm");

        //추출 한 값으로 MarkerUpdateDTO 생성
        MarkerUpdateDTO markerUpdateDTO = new MarkerUpdateDTO(markerId, confirm);

        //마커 update는 확정변경만 존재한다.
        MarkerResponseDTO updatedMarker = markerService.update(markerUpdateDTO);
        //WebSocketMessageConverter를 사용해 메시지 객체 생성
        WebSocketMessageConverter<MarkerResponseDTO> updateConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<MarkerResponseDTO> updatedMarkerMessage =
                updateConverter.createMessage("UPDATED_MARKER", updatedMarker);

        //
        if(confirm) {addSchedule(roomId, markerId);}

        log.debug("UpdatedMarkerMessage: " + updatedMarkerMessage);
        template.convertAndSend("/topic/room/" + roomId + "/map", updatedMarkerMessage);

    }

    private void addSchedule(String roomId, Long markerId) {

        ScheduleItemResponseDTO foundScheduleItem =  scheduleItemService.readByMarkerId(markerId);

        //WebSocketMessageConverter를 사용해 메시지 객체 생성
        WebSocketMessageConverter<ScheduleItemResponseDTO> foundConverter = new WebSocketMessageConverter<>();
        WebSocketMessageConverter.WebsocketMessage<ScheduleItemResponseDTO> createdScheduleItemMessage =
                foundConverter.createMessage("ADDED_SCHEDULEITEM", foundScheduleItem);

        log.debug("CreatedScheduleItemMessage: " + createdScheduleItemMessage);
        //생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
        template.convertAndSend("/topic/room/" + roomId + "/schedule", createdScheduleItemMessage);

    }

    private void deleteMarker(String roomId, Map<String, Object> data) {

        //클라이언트가 송신한 메시지의 data에서 markerId 값을 추출
        Long deleteMarkerId = ((Number) data.get("markerId")).longValue();

        //추출한 markerId를 매개변수로 delete 메서드 실행
        markerService.delete(deleteMarkerId);

        //Delete message 생성
        Map<String, Object> deletedMarkerMessage = new LinkedHashMap<>();
        deletedMarkerMessage.put("action", "DELETED_MARKER");
        deletedMarkerMessage.put("data", Map.of(
                "message", "마커가 삭제되었습니다."
        ));
        log.debug("DeletedMarkerMessage: " + deletedMarkerMessage);
        template.convertAndSend("/topic/room/" + roomId + "/map", deletedMarkerMessage);
    }


}
