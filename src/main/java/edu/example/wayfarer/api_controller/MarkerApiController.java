package edu.example.wayfarer.api_controller;

import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.marker.MarkerUpdateDTO;
import edu.example.wayfarer.service.MarkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 테스트용 임시 컨트롤러 입니다.
@RestController
@RequestMapping("/api/marker")
@RequiredArgsConstructor
public class MarkerApiController {

    private final MarkerService markerService;

    @PostMapping
    public ResponseEntity<MarkerResponseDTO> createMarker(
            @RequestBody MarkerRequestDTO markerRequestDTO
    ) {
        return ResponseEntity.ok(markerService.create(markerRequestDTO));
    }

    @GetMapping("/{markerId}")
    public ResponseEntity<MarkerResponseDTO> readMarker(
            @PathVariable Long markerId
    ) {
        return ResponseEntity.ok(markerService.read(markerId));
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<MarkerResponseDTO>> readMarkers(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(markerService.getListBySchedule(scheduleId));
    }

    @PutMapping
    public ResponseEntity<MarkerResponseDTO> updateMarker(
            @RequestBody MarkerUpdateDTO markerUpdateDTO
    ) {
        return ResponseEntity.ok(markerService.update(markerUpdateDTO));
    }

    @DeleteMapping("/{markerId}")
    public ResponseEntity<Map<String, String>> deleteMarker(
            @PathVariable Long markerId
    ) {
        markerService.delete(markerId);
        return ResponseEntity.ok(Map.of("message", "success delete"));
    }





}

