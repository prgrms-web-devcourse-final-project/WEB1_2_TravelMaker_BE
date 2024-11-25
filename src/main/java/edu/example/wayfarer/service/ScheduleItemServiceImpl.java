package edu.example.wayfarer.service;

import edu.example.wayfarer.converter.ScheduleItemConverter;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import edu.example.wayfarer.entity.Marker;
import edu.example.wayfarer.entity.ScheduleItem;
import edu.example.wayfarer.entity.enums.Color;
import edu.example.wayfarer.exception.MarkerException;
import edu.example.wayfarer.exception.ScheduleItemException;
import edu.example.wayfarer.repository.MarkerRepository;
import edu.example.wayfarer.repository.MemberRoomRepository;
import edu.example.wayfarer.repository.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleItemServiceImpl implements ScheduleItemService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final MarkerRepository markerRepository;
    private final MemberRoomRepository memberRoomRepository;

    /**
     * 스케쥴 아이템 조회 메서드
     * scheduleItemId로 ScheduleItem 조회 후 ScheduleItemResponseDTO 로 변환하여 반환
     *
     * @param scheduleItemId 조회할 ScheduleItem 의 PK
     * @return ScheduleItemResponseDTO 조회된 ScheduleItem 의 응답 데이터
     */
    @Override
    public ScheduleItemResponseDTO read(Long scheduleItemId) {
        // scheduleItemId 로 scheduleItem 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // 조회된 scheduleItem 을 ScheduleItemResponseDTO 로 변환 후 반환
        return ScheduleItemConverter.toScheduleItemResponseDTO(scheduleItem);
    }

    /**
     * 스케쥴 아이템 목록 조회
     * scheduleId 를 기준으로 ScheduleItem 조회 후 ScheduleItemResponseDTO 리스트로 변환하여 반환
     *
     * @param scheduleId ScheduleItem 을 조회할 기준
     * @return List<ScheduleItemResponseDTO> 조회된 ScheduleItem 리스트의 응답 데이터
     */
    @Override
    public List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId) {
        // scheduleId 를 기준으로 scheduleItem 리스트 조회
        List<ScheduleItem> scheduleItems = scheduleItemRepository.findByMarker_Schedule_ScheduleId(scheduleId);

        // 조회된 ScheduleItem 리스트를 ScheduleItemResponseDTO 리스트로 변환하여 반환
        return scheduleItems.stream()
                .map(ScheduleItemConverter::toScheduleItemResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * ScheduleItem 수정 메서드
     * UpdateDTO 의 null 이 아닌 값만 엔티티에 반영
     *
     * @param scheduleItemUpdateDTO 수정할 데이터가 담긴 DTO
     * @return ScheduleItemResponseDTO 수정된 ScheduleItem 의 응답 데이터
     */
    @Override
    public ScheduleItemResponseDTO update(ScheduleItemUpdateDTO scheduleItemUpdateDTO) {
        // 수정한 ScheduleItem 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemUpdateDTO.scheduleItemId())
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // name 수정
        if (scheduleItemUpdateDTO.name() != null) {
            scheduleItem.changeName(scheduleItemUpdateDTO.name());
        }
        // content 수정
        if (scheduleItemUpdateDTO.content() != null) {
            scheduleItem.changeContent(scheduleItemUpdateDTO.content());
        }

        // 수정한 ScheduleItem 저장
        ScheduleItem savedScheduleItem = scheduleItemRepository.save(scheduleItem);
        // 수정된 ScheduleItem 을 ScheduleItemResponseDTO 로 변환하여 반환
        return ScheduleItemConverter.toScheduleItemResponseDTO(savedScheduleItem);
    }

    /**
     * ScheduleItem 삭제 메서드
     * ScheduleItem 을 삭제하고
     * 부모 Marker 의 confirm 을 false 로, color 를 작성자 color 로 변경
     *
     * @param scheduleItemId 삭제할 ScheduleItem 의 PK
     */
    @Override
    public void delete(Long scheduleItemId) {
        // 삭제할 ScheduleItem 의 부모 Marker 조회
        Marker foundMarker = markerRepository.findByScheduleItem_ScheduleItemId(scheduleItemId)
                .orElseThrow(MarkerException.NOT_FOUND::get);

        // Marker 자식 관계 끊고 orphanRemoval = true 를 이용해 자동 삭제
        foundMarker.changeScheduleItem(null);

        // Marker 의 confirm 을 false 로 변경
        foundMarker.changeConfirm(false);

        // Marker 의 color 를 작성자의 색상으로 변경
        foundMarker.changeColor(
                findColor(
                        foundMarker.getMember().getEmail(),
                        foundMarker.getSchedule().getRoom().getRoomId()
                )
        );
        
        // 변경사항 저장
        markerRepository.save(foundMarker);
    }

    // 마커 작성자의 color 조회 메서드
    private Color findColor(String email, String roomId) {
        // 특정 방 사용자의 color 값 가져오기
        return memberRoomRepository.findByMember_EmailAndRoom_RoomId(email, roomId)
                .orElseThrow(()-> new RuntimeException("memberRoom not found"))
                .getColor();
    }


}
