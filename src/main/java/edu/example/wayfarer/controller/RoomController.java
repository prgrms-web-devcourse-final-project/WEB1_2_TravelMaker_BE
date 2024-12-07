package edu.example.wayfarer.controller;

import edu.example.wayfarer.annotation.DeleteRoomOperation;
import edu.example.wayfarer.annotation.ForcedExitOperation;
import edu.example.wayfarer.annotation.LeaveOperation;
import edu.example.wayfarer.auth.util.SecurityUtil;
import edu.example.wayfarer.dto.memberRoom.MemberRoomForceDeleteDTO;
import edu.example.wayfarer.dto.responses.DeleteResponse;
import edu.example.wayfarer.dto.responses.LeaveResponse;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.service.MemberRoomService;
import edu.example.wayfarer.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/room")
public class RoomController {
    private final RoomService roomService;
    private final MemberRoomService memberRoomService;
    private final SecurityUtil securityUtil;
    private final SimpMessagingTemplate template;
    // 타임스탬프 포맷 정의 (밀리세컨드까지 포함)
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss.SSS z yyyy", Locale.ENGLISH);



    @Operation(summary = "단일 방 정보 조회", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다", content = @Content),
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO> readRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(roomService.read(roomId));
    }

    @Operation(summary = "방 정보 수정(방장만 가능)", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없습니다", content = @Content),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다", content = @Content)
    })
    @PutMapping
    public ResponseEntity<RoomResponseDTO> updateRoom(@RequestBody RoomUpdateDTO roomUpdateDTO) {
        // 로그인한 사용자가 해당 방의 방장이 맞는지 아닌지 확인
        Member currentUser = securityUtil.getCurrentUser();
        RoomUpdateDTO updatedDTO = new RoomUpdateDTO(
                roomUpdateDTO.roomId(),
                roomUpdateDTO.title(),
                roomUpdateDTO.country(),
                roomUpdateDTO.startDate(),
                roomUpdateDTO.endDate()
        );
        return ResponseEntity.ok(roomService.update(updatedDTO, currentUser.getEmail()));
    }

    @DeleteRoomOperation
    @DeleteMapping("/{roomId}")
    public ResponseEntity<DeleteResponse> deleteRoom(@PathVariable String roomId) {
        Member currentUser = securityUtil.getCurrentUser();
        roomService.delete(currentUser, roomId);

        DeleteResponse response = new DeleteResponse("삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @LeaveOperation
    @DeleteMapping("/leave/{roomId}")
    public ResponseEntity<LeaveResponse> leaveRoom(@PathVariable String roomId) {
        Member currentUser = securityUtil.getCurrentUser();
        memberRoomService.delete(currentUser, roomId);

        //웹소켓 메시지 브로드캐스팅
        Map<String, Object> leaveMessage = new LinkedHashMap<>();
        leaveMessage.put("action", "LEAVE_MESSAGE");
        leaveMessage.put("data", Map.of(
                        "message", currentUser.getEmail()
                )
        );

        template.convertAndSend("/topic/room/" + roomId+"/member", leaveMessage);

        LeaveResponse response = new LeaveResponse("퇴장하였습니다.");
        return ResponseEntity.ok(response);
    }

    // 강제퇴장
    @ForcedExitOperation
    @DeleteMapping("/forcedExit")
    public ResponseEntity<LeaveResponse> forcedExit(@RequestBody MemberRoomForceDeleteDTO forceDeleteDTO) {
        Member currentUser = securityUtil.getCurrentUser();
        memberRoomService.forceDelete(forceDeleteDTO, currentUser);

        //웹소켓 메시지 브로드캐스팅
        Map<String, Object> forcedExitMessage = new LinkedHashMap<>();
        forcedExitMessage.put("action", "FORCE_EXIT");
        forcedExitMessage.put("data", Map.of("message", forceDeleteDTO.deletingEmail())
        );

        template.convertAndSend("/topic/room/" + forceDeleteDTO.roomId()+"/member", forcedExitMessage);
        LeaveResponse response = new LeaveResponse("강제퇴장하였습니다.");
        return ResponseEntity.ok(response);
    }

}
