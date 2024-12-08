package edu.example.wayfarer.controller;

import edu.example.wayfarer.annotation.JoinOperation;
import edu.example.wayfarer.auth.util.SecurityUtil;
import edu.example.wayfarer.dto.memberRoom.MemberRoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.service.MemberRoomService;
import edu.example.wayfarer.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/room")
public class MainController {

    private final RoomService roomService;
    private final MemberRoomService memberRoomService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "방 생성", responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "날짜를 제대로 입력해주세요. \t\n 여행기간 설정은 30일까지 가능합니다.", content = @Content),
    })
    @PostMapping
    public RoomResponseDTO createRoom(@RequestBody RoomRequestDTO roomRequestDTO) {
        Member currentUser = securityUtil.getCurrentUser();
        RoomRequestDTO updatedDTO = new RoomRequestDTO(
                roomRequestDTO.title(),
                roomRequestDTO.country(),
                roomRequestDTO.startDate(),
                roomRequestDTO.endDate()
        );
        return roomService.create(updatedDTO, currentUser.getEmail());
    }

    @Operation(summary = "방 리스트 조회", responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/list")
    public ResponseEntity<List<RoomListDTO>> getListByEmail() {
        Member currentUser = securityUtil.getCurrentUser();
        List<RoomListDTO> rooms = memberRoomService.listByEmail(currentUser);
        return ResponseEntity.ok(rooms);
    }

    @JoinOperation
    @PostMapping("/join")
    public ResponseEntity<Map<String,String>> createMemberRoom(@RequestBody MemberRoomRequestDTO memberRoomRequestDTO) {
        Member currentUser = securityUtil.getCurrentUser();
        MemberRoomRequestDTO updatedDTO = new MemberRoomRequestDTO(
                memberRoomRequestDTO.roomId(),
                memberRoomRequestDTO.roomCode()
        );
        memberRoomService.create(updatedDTO, currentUser.getEmail());
        return ResponseEntity.ok(Map.of("message", "방에 입장했습니다."));
    }

}
