package edu.example.wayfarer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.converter.WebSocketMessageConverter;
import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.marker.MarkerUpdateDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.repository.ScheduleItemRepository;
import edu.example.wayfarer.service.MarkerService;
import edu.example.wayfarer.service.ScheduleItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MarkerController {
    private final SimpMessagingTemplate template;
    private final MarkerService markerService;
    private final ScheduleItemService scheduleItemService;

    @MessageMapping("room/{roomId}/map")
    public void handleMarker(
            @DestinationVariable String roomId,
            @Payload Map<String, Object> markerPayload,
            StompHeaderAccessor headerAccessor
    ) {

        String action = (String) markerPayload.get("action");
        /*
        클라이언트에서 송신한 메시지의 "action" 항목으로 기능 구분
        MarkerService의 메서드 리턴값인 MarkerResponseDTO의 내용으로
        클라이언트에 브로드캐스팅 할 JSON 메시지 구현
        */

        switch (action) {
            case "ADD_MARKER":

                //클라이언트가 송신한 메시지인 markerPayload에서 scheduleId, lat, lng 값을 추출
                Long scheduleId = ((Number) ((Map<String, Object>) markerPayload.get("data")).get("scheduleId")).longValue();
                Double lat = ((Number) ((Map<String, Object>) markerPayload.get("data")).get("lat")).doubleValue();
                Double lng = ((Number) ((Map<String, Object>) markerPayload.get("data")).get("lng")).doubleValue();

                //세션에 저장해놓은 email 값을 추출
//                Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
//                if (sessionAttributes == null || !sessionAttributes.containsKey("email")) {
//                    throw new IllegalArgumentException("Email not found in session");
//                }
//                String email = (String) sessionAttributes.get("email");

                String email = "member1@abc.com";

                //추출 한 값으로 MarkerRequestDTO 생성
                MarkerRequestDTO markerRequestDTO = new MarkerRequestDTO(email,scheduleId,lat,lng);
                //MarkerRequestDTO로 markService.create() 실행
                MarkerResponseDTO addedMarker = markerService.create(markerRequestDTO);
                //WebSocketMessageConverter를 사용해 메시지 객체 생성
                WebSocketMessageConverter<MarkerResponseDTO> addConverter = new WebSocketMessageConverter<>();
                WebSocketMessageConverter.WebsocketMessage<MarkerResponseDTO> addedMarkerMessage =
                        addConverter.createMessage("ADDED_MARKER", addedMarker);

                System.out.println(addedMarkerMessage);
                //생성한 메시지를 "topic/schedule/{roomId}/map" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
                template.convertAndSend("/topic/room/" + roomId + "/map", addedMarkerMessage);
                break;

            case "UPDATE_MARKER":

                //클라이언트가 송신한 메시지인 markerPayload에서 markerId, confirm 값을 추출
                Long markerId = ((Number) ((Map<String, Object>) markerPayload.get("data")).get("markerId")).longValue();
                Boolean confirm = (Boolean) ((Map<String, Object>) markerPayload.get("data")).get("confirm");

                //추출 한 값으로 MarkerUpdateDTO 생성
                MarkerUpdateDTO markerUpdateDTO = new MarkerUpdateDTO(markerId, confirm);

                //마커 update는 확정변경만 존재한다.
                MarkerResponseDTO updatedMarker = markerService.update(markerUpdateDTO);
                //WebSocketMessageConverter를 사용해 메시지 객체 생성
                WebSocketMessageConverter<MarkerResponseDTO> updateConverter = new WebSocketMessageConverter<>();
                WebSocketMessageConverter.WebsocketMessage<MarkerResponseDTO> updatedMarkerMessage =
                        updateConverter.createMessage("UPDATED_MARKER", updatedMarker);


                /*
                전달 받은 confirm값이 true 이면
                markerService.update() 과정에서 등록된 scheduleItem의 값을 받아와
                ADDED_SCHEDULE 액션의 메시지를 송신한다
                 */
                if(confirm) {
                    ScheduleItemResponseDTO foundScheduleItem =  scheduleItemService.readByMarkerId(markerId);

                    //WebSocketMessageConverter를 사용해 메시지 객체 생성
                    WebSocketMessageConverter<ScheduleItemResponseDTO> foundConverter = new WebSocketMessageConverter<>();
                    WebSocketMessageConverter.WebsocketMessage<ScheduleItemResponseDTO> createdScheduleItemMessage =
                            foundConverter.createMessage("ADDED_SCHEDULE", foundScheduleItem);

                    System.out.println("createdScheduleItemMessage: " + createdScheduleItemMessage);
                    //생성한 메시지를 "topic/schedule/{roomId}/schedule" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
                    template.convertAndSend("/topic/room/" + roomId + "/schedule", createdScheduleItemMessage);
                }

                System.out.println("updatedMarkerMessage: " + updatedMarkerMessage);
                //생성한 메시지를 "topic/schedule/{roomId}/map" 을 구독한 클라이언트들에게 브로드캐스팅합니다.
                template.convertAndSend("/topic/room/" + roomId + "/map", updatedMarkerMessage);
                break;


            case "DELETE_MARKER":
                //클라이언트가 송신한 메시지인 markerPayload에서 markerId 값을 추출
                Long deleteMarkerId = ((Number) ((Map<String, Object>) markerPayload.get("data")).get("markerId")).longValue();
                //추출한 markerId를 매개변수로 delete 메서드 실행
                markerService.delete(deleteMarkerId);
                Map<String, Object> deletedMarkerMessage = new LinkedHashMap<>();
                deletedMarkerMessage.put("action", "DELETED_MARKER");
                deletedMarkerMessage.put("data", Map.of(
                        "message", "마커가 삭제되었습니다."
                ));
                System.out.println(deletedMarkerMessage);
                template.convertAndSend("/topic/room/" + roomId + "/map", deletedMarkerMessage);
                break;

            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }


    }
}
