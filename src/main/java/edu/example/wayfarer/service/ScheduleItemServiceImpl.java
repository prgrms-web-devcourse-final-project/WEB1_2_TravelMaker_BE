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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
//    @Transactional(readOnly = true)
    public List<ScheduleItemResponseDTO> getListBySchedule(Long scheduleId) {
        // 1. scheduleId 를 기준으로 scheduleItem 리스트 조회
        List<ScheduleItem> scheduleItems
                = scheduleItemRepository.findByMarkerScheduleScheduleId(scheduleId);

        // 2. LinkedList 를 구성하기 위해 시작 아이템 찾기
        ScheduleItem startItem = scheduleItems.stream()
                .filter(item -> item.getPreviousItem() == null)
                .findFirst()
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // 3. LinkedList 형태로 순회하며 순차적인 index 부여
        int index = 0;

        List<ScheduleItemResponseDTO> orderedList = new ArrayList<>();

        // 순회 시작 아이템
        ScheduleItem currentItem = startItem;

        while (currentItem != null) { // 리스트 순회
            orderedList.add(
                    ScheduleItemConverter.toScheduleItemResponseDTO(
                            currentItem, index++
                    )
            );
            // 다음 아이템이로 이동
            currentItem = currentItem.getNextItem();
        }

        return orderedList;
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
//    @Transactional(readOnly = true)
    public Page<ScheduleItemResponseDTO> getPageBySchedule(Long scheduleId, PageRequestDTO pageRequestDTO) {
        // 1. 시작 아이템 조회
        ScheduleItem startItem
                = scheduleItemRepository.findFirstByMarkerScheduleScheduleIdAndPreviousItemIsNull(scheduleId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // 2. LinkedList 형태로 순회하며 필요한 데이터 가져오기
        List<ScheduleItemResponseDTO> orderedList = new ArrayList<>();

        ScheduleItem currentItem = startItem;

        // 인덱스 초기값
        int index = 0;
        // 페이징 시작점
        int skipCount = (pageRequestDTO.page() - 1) * pageRequestDTO.size();

        while (currentItem != null) {
            if (index >= skipCount && orderedList.size() < pageRequestDTO.size()) {
                orderedList.add(
                        ScheduleItemConverter.toScheduleItemResponseDTO(
                               currentItem, index
                        )
                );
            }
            if (orderedList.size() >= pageRequestDTO.size()) {
                break;
            }
            currentItem = currentItem.getNextItem();
            index++;
        }

        // 3. 전체 데이터 크기 조회
        long totalItems = scheduleItemRepository.countByScheduleId(scheduleId);

        // 4. Page 객체 반환
        Pageable pageable = pageRequestDTO.getPageable(Sort.unsorted());

        return new PageImpl<>(orderedList, pageable, totalItems);
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
        // 순서 수정
        if (scheduleItemUpdateDTO.previousItemId() != null || scheduleItemUpdateDTO.nextItemId() != null) {
            // LinkedList 재설정 메서드 호출
            updateIndex(
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
    @Transactional
    public void delete(Long scheduleItemId) {
        // 1. 삭제할 scheduleItem 조회
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // 2. 삭제할 scheduleItem 의 앞과 뒤 scheduleItem
        ScheduleItem previousItem = scheduleItem.getPreviousItem();
        ScheduleItem nextItem = scheduleItem.getNextItem();

        // 3. LinkedList 연결 재설정
        //  - 앞 scheduleItem 의 nextItem 을
        //    삭제할 scheduleItem 의 nextItem 으로 변경
        if (previousItem != null) {
            previousItem.changeNextItem(nextItem);
        }

        //  - 뒤 scheduleItem 의 previousItem 을
        //    삭제할 scheduleItem 의 previousItem 으로 변경
        if (nextItem != null) {
            nextItem.changePreviousItem(previousItem);
        }

        // 4. 삭제할 ScheduleItem 의 부모 Marker 조회
        Marker foundMarker = markerRepository.findByScheduleItemScheduleItemId(scheduleItemId)
                .orElseThrow(MarkerException.NOT_FOUND::get);

        // 5. 스케쥴아이템 삭제
        foundMarker.changeScheduleItem(null);

        // 6. Marker 의 confirm 을 false 로 변경
        foundMarker.changeConfirm(false);

        // 7. Marker 의 color 를 작성자의 색상으로 변경
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


    @Override
    public ScheduleItemResponseDTO readByMarkerId(Long markerId) {
        Optional<ScheduleItem> scheduleItem = scheduleItemRepository.findByMarkerMarkerId(markerId);
        ScheduleItem item = scheduleItem.orElseThrow(() -> new IllegalArgumentException("해당 마커 ID에 해당하는 ScheduleItem이 존재하지 않습니다: " + markerId));
        int itemIndex = getIndex(scheduleItem.get().getScheduleItemId(), scheduleItem.get().getMarker().getSchedule().getScheduleId());
        return ScheduleItemConverter.toScheduleItemResponseDTO(item, itemIndex);
    }

    // LinkedList 구조 재설정 메서드
    @Transactional
    protected void updateIndex(ScheduleItem scheduleItem, Long previousItemId, Long nextItemId) {
        // 기존 연결 끊기
        if (scheduleItem.getPreviousItem() != null) {
            scheduleItem.getPreviousItem().changeNextItem(scheduleItem.getNextItem());
        }
        if (scheduleItem.getNextItem() != null) {
            scheduleItem.getNextItem().changePreviousItem(scheduleItem.getPreviousItem());
        }

        if (previousItemId != null && nextItemId != null) {
            // CASE1: 두개의 일정 사이로 이동할 경우
            // 1. 앞에 위치할 ScheduleItem 조회
            ScheduleItem previousItem = scheduleItemRepository.findById(previousItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);
            // 2. 뒤에 이치할 ScheduleItem 조회
            ScheduleItem nextItem = scheduleItemRepository.findById(nextItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 3. 두개의 아이템 사이에 다른 아이템이 없는지 체크
            if (!previousItem.getNextItem().getScheduleItemId().equals(nextItem.getScheduleItemId())) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 4. LinkedList  구조 재설정
            previousItem.changeNextItem(scheduleItem);
            scheduleItem.changePreviousItem(previousItem);
            scheduleItem.changeNextItem(nextItem);
            nextItem.changePreviousItem(scheduleItem);


        } else if (previousItemId != null) {
            // CASE2: 제일 뒤로 이동할 경우
            // 1. 앞에 위치할 ScheduleItem 조회
            ScheduleItem previousItem = scheduleItemRepository.findById(previousItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 2. 조회한 객체가 제일 마지막 순서 인지 체크
            if (previousItem.getNextItem() != null) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 3. LinkedList 구조 재설정
            previousItem.changeNextItem(scheduleItem);
            scheduleItem.changePreviousItem(previousItem);
            scheduleItem.changeNextItem(null);

        } else if (nextItemId != null) {
            // CASE3: 제일 앞으로 이동할 경우
            // 1. 뒤에 위치할 ScheduleItem 조회
            ScheduleItem nextItem = scheduleItemRepository.findById(nextItemId)
                    .orElseThrow(ScheduleItemException.NOT_FOUND::get);

            // 2. 조회한 객체가 제일 첫번째 순서 인지 체크
            if (nextItem.getPreviousItem() != null) {
                throw ScheduleItemException.IDS_INVALID.get();
            }

            // 3. LinkedList 구조 재설정
            nextItem.changePreviousItem(scheduleItem);
            scheduleItem.changePreviousItem(null);
            scheduleItem.changeNextItem(nextItem);

        } else {
            throw ScheduleItemException.INVALID_REQUEST.get();
        }
    }

    // scheduleItem 의 index 를 구하는 메서드
//    @Transactional(readOnly = true)
    public int getIndex(Long scheduleItemId, Long scheduleId) {
        // scheduleId 를 가지는 첫번째 scheduleItem 조회
        ScheduleItem startItem
                = scheduleItemRepository.findFirstByMarkerScheduleScheduleIdAndPreviousItemIsNull(scheduleId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);

        // 시작 index 값
        int index = 0;

        // LinkedList 를 순회하며 목표 scheduleItem 찾기
        ScheduleItem currentItem = startItem;

        while (currentItem != null) {
            if (currentItem.getScheduleItemId().equals(scheduleItemId)) {
                return index;
            }
            currentItem = currentItem.getNextItem();
            index++;
        }

        // 예외 처리
        throw ScheduleItemException.NOT_FOUND.get();
    }
}
