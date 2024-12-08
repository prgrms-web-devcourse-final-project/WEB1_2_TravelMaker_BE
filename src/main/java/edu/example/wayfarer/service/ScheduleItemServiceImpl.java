package edu.example.wayfarer.service;

import edu.example.wayfarer.converter.ScheduleItemConverter;
import edu.example.wayfarer.dto.common.PageRequestDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemUpdateDTO;
import edu.example.wayfarer.entity.Marker;
import edu.example.wayfarer.entity.ScheduleItem;
import edu.example.wayfarer.entity.enums.Color;
import edu.example.wayfarer.exception.MarkerException;
import edu.example.wayfarer.exception.ScheduleItemException;
import edu.example.wayfarer.manager.ScheduleItemOrderManager;
import edu.example.wayfarer.repository.MarkerRepository;
import edu.example.wayfarer.repository.MemberRoomRepository;
import edu.example.wayfarer.repository.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleItemServiceImpl implements ScheduleItemService {

    private final ScheduleItemRepository scheduleItemRepository;
    private final MarkerRepository markerRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final ScheduleItemOrderManager scheduleItemOrderManager;

    /**
     * 스케쥴 아이템 조회 메서드 1
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

        // scheduleItem 의 index 를 구하는 메서드 호출
        int itemOrderIndex = scheduleItemOrderManager.getIndex(scheduleItem);

        // 조회된 scheduleItem 과 index 를 ScheduleItemResponseDTO 로 변환 후 반환
        return ScheduleItemConverter.toScheduleItemResponseDTO(scheduleItem, itemOrderIndex);
    }

    /**
     * 스케쥴 아이템 조회 메서드 2
     * markerId로 ScheduleItem 조회 후 ScheduleItemResponseDTO 로 변환하여 반환
     *
     * @param markerId 조회할 ScheduleItem 의 markerId
     * @return ScheduleItemResponseDTO 조회된 ScheduleItem 의 응답 데이터
     */
    @Override
    public ScheduleItemResponseDTO readByMarkerId(Long markerId) {
        // markerId 로 scheduleItem 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findByMarkerMarkerId(markerId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // scheduleItem 의 index 를 구하는 메서드 호출
        int itemOrderIndex = scheduleItemOrderManager.getIndex(scheduleItem);
        return ScheduleItemConverter.toScheduleItemResponseDTO(scheduleItem, itemOrderIndex);
    }

    /**
     * 스케쥴 아이템 목록 조회 메서드 1
     * scheduleId 를 기준으로 ScheduleItem 조회 후 ScheduleItemResponseDTO 리스트로 변환하여 반환
     *
     * @param scheduleId ScheduleItem 을 조회할 기준
     * @return List<ScheduleItemResponseDTO> 조회된 ScheduleItem 리스트의 응답 데이터
     */
    @Override
//    @Transactional(readOnly = true)
    public List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId) {
        return scheduleItemOrderManager.getOrderedItems(scheduleId);
    }

    /**
     * 스케쥴 아이템 목록 조회 메서드 2
     * scheduleId 를 기준으로 ScheduleItem 조회 후 ScheduleItemResponseDTO 페이지로 변환하여 반환
     *
     * @param scheduleId ScheduleItem 을 조회할 기준
     * @param pageRequestDTO page, size 정보
     * @return Page<ScheduleItemResponseDTO> 조회된 ScheduleItem 페이지의 응답 데이터
     */
    @Override
//    @Transactional(readOnly = true)
    public Page<ScheduleItemResponseDTO> getPageBySchedule(Long scheduleId, PageRequestDTO pageRequestDTO) {
        // 페이지에 들어갈 아이템의 목록
        List<ScheduleItemResponseDTO> pageItems = scheduleItemOrderManager.getPaginatedItems(scheduleId, pageRequestDTO);

        // 전체 데이터 크기 조회
        long totalItems = scheduleItemRepository.countByScheduleId(scheduleId);

        // Pageable 생성
        Pageable pageable = pageRequestDTO.getPageable(Sort.unsorted());

        return new PageImpl<>(pageItems, pageable, totalItems);
    }

    /**
     *  ScheduleItem 수정 메서드
     * UpdateDTO 의 null 이 아닌 값만 엔티티에 반영
     *
     * @param scheduleItemUpdateDTO 수정할 데이터가 담긴 DTO
     * @return ScheduleItemResponseDTO 수정된 ScheduleItem 의 응답 데이터
     */
    @Override
    @Transactional
    public ScheduleItemResponseDTO update(ScheduleItemUpdateDTO scheduleItemUpdateDTO) {
        // 수정할 ScheduleItem 조회
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
        // 순서 수정
        if (scheduleItemUpdateDTO.previousItemId() != null || scheduleItemUpdateDTO.nextItemId() != null) {
            // LinkedList 재설정 메서드 호출
            scheduleItemOrderManager.updateOrder(
                    scheduleItem,
                    scheduleItemUpdateDTO.previousItemId(),
                    scheduleItemUpdateDTO.nextItemId()
            );
        }

        // 수정한 ScheduleItem 저장
        ScheduleItem savedScheduleItem = scheduleItemRepository.save(scheduleItem);

        // ScheduleItem 의 index 를 구하는 메서드 호출
        int itemOrderIndex = scheduleItemOrderManager.getIndex(savedScheduleItem);

        // 수정된 ScheduleItem 과 index 를 ScheduleItemResponseDTO 로 변환하여 반환
        return ScheduleItemConverter.toScheduleItemResponseDTO(savedScheduleItem, itemOrderIndex);
    }

    /**
     * ScheduleItem 삭제 메서드
     * ScheduleItem 을 삭제하고
     * 부모 Marker 의 confirm 을 false 로, color 를 작성자 color 로 변경
     *
     * @param scheduleItemId 삭제할 ScheduleItem 의 PK
     */
    @Override
    @Transactional
    public void delete(Long scheduleItemId) {
        // 삭제할 scheduleItem 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // LinkedList 연결 제거 메서드 호출
        scheduleItemOrderManager.detachItem(scheduleItem);

        // 삭제할 ScheduleItem 의 부모 Marker 조회
        Marker foundMarker = markerRepository.findByScheduleItemScheduleItemId(scheduleItemId)
                .orElseThrow(MarkerException.NOT_FOUND::get);
        // 스케쥴아이템 삭제
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
    }

    // 마커 작성자의 color 조회 메서드
    private Color findColor(String email, String roomId) {
        // 특정 방 사용자의 color 값 가져오기
        return memberRoomRepository.findByMemberEmailAndRoomRoomId(email, roomId)
                .orElseThrow(()-> new RuntimeException("memberRoom not found"))
                .getColor();
    }
}
