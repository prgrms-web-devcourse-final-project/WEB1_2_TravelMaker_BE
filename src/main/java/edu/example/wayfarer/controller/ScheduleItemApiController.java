//package edu.example.wayfarer.controller;
//
//import edu.example.wayfarer.dto.common.PageRequestDTO;
//import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
//import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
//import edu.example.wayfarer.service.ScheduleItemService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//// 테스트용 임시 컨트롤러 입니다.
//@RestController
//@RequestMapping("/api/scheduleItem")
//@RequiredArgsConstructor
//public class ScheduleItemApiController {
//    private final ScheduleItemService scheduleItemService;
//
//    @GetMapping("/{scheduleItemId}")
//    public ResponseEntity<ScheduleItemResponseDTO> readScheduleItem(
//            @PathVariable Long scheduleItemId) {
//        return ResponseEntity.ok(scheduleItemService.read(scheduleItemId));
//    }
//
//    @GetMapping("/list/schedule/{scheduleId}")
//    public ResponseEntity<List<ScheduleItemResponseDTO>> readScheduleItems(
//            @PathVariable Long scheduleId
//    ) {
//        return ResponseEntity.ok(scheduleItemService.getListBySchedule(scheduleId));
//    }
//
//    @GetMapping("/page/schedule/{scheduleId}")
//    public ResponseEntity<Page<ScheduleItemResponseDTO>> readPage(
//            @PathVariable Long scheduleId,
//            @Validated PageRequestDTO pageRequestDTO
//    ) {
//        return ResponseEntity.ok(scheduleItemService.getPageBySchedule(scheduleId, pageRequestDTO));
//    }
//
//    @PutMapping
//    public ResponseEntity<ScheduleItemResponseDTO> updateScheduleItem(
//            @RequestBody ScheduleItemUpdateDTO scheduleItemUpdateDTO
//    ) {
//        return ResponseEntity.ok(scheduleItemService.update(scheduleItemUpdateDTO));
//    }
//
//    @DeleteMapping("/{scheduleItemId}")
//    public ResponseEntity<Map<String, String>> deleteScheduleItem(
//            @PathVariable Long scheduleItemId
//    ) {
//        scheduleItemService.delete(scheduleItemId);
//        return ResponseEntity.ok(Map.of("message", "success delete"));
//    }
//
//
//}
//
//
