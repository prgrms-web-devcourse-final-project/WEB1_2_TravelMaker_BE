package edu.example.wayfarer.apiController;

import edu.example.wayfarer.dto.room.RoomListDTO;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.service.MemberRoomService;
import edu.example.wayfarer.service.RoomService;
import lombok.RequiredArgsConstructor;
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

    // 방 생성
    @PostMapping
    public ResponseEntity<RoomResponseDTO> createRoom(@RequestBody RoomRequestDTO roomRequestDTO) {
        return ResponseEntity.ok(roomService.create(roomRequestDTO));
    }

    // 방 정보 수정
    @PutMapping
    public ResponseEntity<RoomResponseDTO> updateRoom(@RequestBody RoomUpdateDTO roomUpdateDTO) {
        return ResponseEntity.ok(roomService.update(roomUpdateDTO));
    }

    // 방 삭제
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Map<String, String>> deleteRoom(@PathVariable("roomId") String roomId) {
        roomService.delete(roomId);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }

    // 방리스트 조회
    @GetMapping("/{email}")
    public ResponseEntity<List<RoomListDTO>> getListByEmail(@PathVariable("email") String email) {
        List<RoomListDTO> rooms = memberRoomService.listByEmail(email);
        return ResponseEntity.ok(rooms);
    }


}
