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
import edu.example.wayfarer.repository.MarkerRepository;
import edu.example.wayfarer.repository.MemberRoomRepository;
import edu.example.wayfarer.repository.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

        // scheduleItem 의 index 를 구하는 메서드 호출
        int itemOrderIndex = getIndex(scheduleItemId, scheduleItem.getMarker().getSchedule().getScheduleId());

        // 조회된 scheduleItem 과 index 를 ScheduleItemResponseDTO 로 변환 후 반환
        return ScheduleItemConverter.toScheduleItemResponseDTO(scheduleItem, itemOrderIndex);
    }

    /**
     * 스케쥴 아이템 목록 조회 1
     * scheduleId 를 기준으로 ScheduleItem 조회 후 ScheduleItemResponseDTO 리스트로 변환하여 반환
     *
     * @param scheduleId ScheduleItem 을 조회할 기준
     * @return List<ScheduleItemResponseDTO> 조회된 ScheduleItem 리스트의 응답 데이터
     */
    @Override
    public List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId) {
        // scheduleId 를 기준으로 scheduleItem 리스트 조회
        List<ScheduleItem> scheduleItems =
                scheduleItemRepository.findByMarker_Schedule_ScheduleIdOrderByItemOrderAsc(scheduleId);

        // 순차적인 정수 index 부여
        AtomicInteger index = new AtomicInteger(0);

        // 조회된 ScheduleItem 리스트를 ScheduleItemResponseDTO 리스트로 변환하여 반환
        return scheduleItems.stream()
                .map(scheduleItem ->
                        ScheduleItemConverter.toScheduleItemResponseDTO(
                                scheduleItem, index.getAndIncrement()
                        )
                )
                .collect(Collectors.toList());
    }

    /**
     * 스케쥴 아이템 목록 조회 2
     * scheduleId 를 기준으로 ScheduleItem 조회 후 ScheduleItemResponseDTO 페이지로 변환하여 반환
     *
     * @param scheduleId ScheduleItem 을 조회할 기준
     * @param pageRequestDTO page, size 정보
     * @return Page<ScheduleItemResponseDTO> 조회된 ScheduleItem 페이지의 응답 데이터
     */
    @Override
    public Page<ScheduleItemResponseDTO> getPageBySchedule(Long scheduleId, PageRequestDTO pageRequestDTO) {
        // Pageable 생성
        Pageable pageable = pageRequestDTO.getPageable(Sort.by("itemOrder").ascending());

        // ScheduleItem 조회
        Page<ScheduleItem> scheduleItems
                = scheduleItemRepository.findByMarker_Schedule_ScheduleId(scheduleId, pageable);

        // 페이지의 시작 인덱스
        // 예)page=1,size=5 일 경우 (0*5) 0부터 시작
        // 예)page=2,size=5 일 경우 (1*5) 5부터 시작
        int startIndex = pageable.getPageNumber() * pageable.getPageSize();

        AtomicInteger index = new AtomicInteger(startIndex);

        return scheduleItems.map(scheduleItem ->
            ScheduleItemConverter.toScheduleItemResponseDTO(
                    scheduleItem, index.getAndIncrement()
            )
        );
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
        // itemOrder(순서) 수정
        if (scheduleItemUpdateDTO.previousItemId() != null || scheduleItemUpdateDTO.nextItemId() != null) {
            // itemOrder 수정 메서드 호출
            updateItemOrder(
                    scheduleItem,
                    scheduleItemUpdateDTO.previousItemId(),
                    scheduleItemUpdateDTO.nextItemId()
            );
        }

        // 수정한 ScheduleItem 저장
        ScheduleItem savedScheduleItem = scheduleItemRepository.save(scheduleItem);

        // ScheduleItem 의 index 를 구하는 메서드 호출
        int itemOrderIndex = getIndex(
                savedScheduleItem.getScheduleItemId(),
                savedScheduleItem.getMarker().getSchedule().getScheduleId()
        );

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


    @Override
    public ScheduleItemResponseDTO readByMarkerId(Long markerId) {
        Optional<ScheduleItem> scheduleItem = scheduleItemRepository.findByMarker_MarkerId(markerId);
        ScheduleItem item = scheduleItem.orElseThrow(() -> new IllegalArgumentException("해당 마커 ID에 해당하는 ScheduleItem이 존재하지 않습니다: " + markerId));
        int itemIndex = getIndex(scheduleItem.get().getScheduleItemId(), scheduleItem.get().getMarker().getSchedule().getScheduleId());
        return ScheduleItemConverter.toScheduleItemResponseDTO(item, itemIndex);
    }

    // itemOrder 수정 메서드
    private void updateItemOrder(ScheduleItem scheduleItem, Long previousItemId, Long nextItemId) {

        Long scheduleId = scheduleItem.getMarker().getSchedule().getScheduleId();
        Double newItemOrder = 0.0;

        if (previousItemId != null && nextItemId != null) {  // 두개의 일정 사이로 이동할 경우
            // 앞에 위치할 ScheduleItem 조회
            ScheduleItem previousItem = scheduleItemRepository.findById(previousItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);
            // 뒤에 이치할 ScheduleItem 조회
            ScheduleItem nextItem = scheduleItemRepository.findById(nextItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 두개의 아이템 사이에 다른 아이템이 없는지 체크
            Boolean exists = scheduleItemRepository.existsBetweenItemOrders(
                    scheduleId,
                    previousItem.getItemOrder(),
                    nextItem.getItemOrder()
            );
            if (exists) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 앞과 뒤의 itemOrder 를 더한 값의 중간 값으로 새로운 itemOrder 생성
            newItemOrder = (previousItem.getItemOrder() + nextItem.getItemOrder()) / 2.0;

        } else if (previousItemId != null) {  // 제일 뒤로 이동할 경우
            // 앞에 위치할 ScheduleItem 조회
            ScheduleItem previousItem = scheduleItemRepository.findById(previousItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 조회한 객체가 제일 마지막 순서 인지 체크
            Double maxItemOrder = scheduleItemRepository.findMaxItemOrderByScheduleId(scheduleId);
            if (previousItem.getItemOrder() < maxItemOrder) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 앞의 itemOrder 의 정수 부분에 1.0 을 더한 값으로 새로운 itemOrder 생성
            newItemOrder = Math.floor(previousItem.getItemOrder()) + 1.0;
        } else if (nextItemId != null) {  // 제일 앞으로 이동할 경우
            // 뒤에 위치할 ScheduleItem 조회
            ScheduleItem nextItem = scheduleItemRepository.findById(nextItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 조회한 객체가 제일 첫번째 순서 인지 체크
            Double minItemOrder = scheduleItemRepository.findMinItemOrderByScheduleId(scheduleId);
            if (nextItem.getItemOrder() > minItemOrder) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 0 과 nextItem.itemOrder 의 중간 값으로 새로운 itemOrder 생성
            newItemOrder = (0.0 + nextItem.getItemOrder()) / 2.0;
        } else {
            throw ScheduleItemException.INVALID_REQUEST.get();
        }

        // scheduleItem 객체의 itemOrder 변경
        scheduleItem.changeItemOrder(newItemOrder);
    }

    // scheduleItem 의 index 를 구하는 메서드
    public int getIndex(Long scheduleItemId, Long scheduleId) {
        return scheduleItemRepository.findIndexByScheduleItemId(scheduleItemId, scheduleId);
    }


}
