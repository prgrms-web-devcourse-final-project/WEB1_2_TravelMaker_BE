package edu.example.wayfarer.manager;

import edu.example.wayfarer.converter.ScheduleItemConverter;
import edu.example.wayfarer.dto.common.PageRequestDTO;
import edu.example.wayfarer.dto.scheduleItem.ScheduleItemResponseDTO;
import edu.example.wayfarer.entity.ScheduleItem;
import edu.example.wayfarer.exception.ScheduleItemException;
import edu.example.wayfarer.repository.ScheduleItemRepository;
import edu.example.wayfarer.util.BreakableBiConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

// LinkedList 기반 순서 관리 구현체
@Component
@RequiredArgsConstructor
public class LinkedListOrderManager implements ScheduleItemOrderManager {

    private final ScheduleItemRepository scheduleItemRepository;

    /**
     * 특정 ScheduleItem 의 인덱스를 계산
     * LinkedList 구조를 따라 순회하며 목표 아이템의 위치를 찾음
     *
     * @param scheduleItem 인덱스를 계산할 ScheduleItem
     * @return 해당 ScheduleItem 의 인덱스(0부터 시작)
     */
    @Override
    public int getIndex(ScheduleItem scheduleItem) {
        // 1. scheduleId 를 가지는 첫번째 scheduleItem 조회
        ScheduleItem startItem = findStartItem(scheduleItem.getMarker().getSchedule().getScheduleId());

        // 2. 시작 index 값
        int index = 0;

        // 3. 조회한 scheduleItem 을 LinkedList 를 순회할 첫 아이템으로 지정
        ScheduleItem currentItem = startItem;

        // 4. LinkedList 구조 순회
        while (currentItem != null) {
            if (currentItem.equals(scheduleItem)) {
                return index;  // 목표 아이템 발견시 인덱스 반환
            }
            currentItem = currentItem.getNextItem(); // 다음 아이템으로 이동
            index++;
        }

        // 예외 처리
        throw ScheduleItemException.NOT_FOUND.get();
    }

    /**
     * ScheduleId를 기반으로 LinkedList 구조를 순회하고
     * 정렬된 ScheduleItemResponseDTO 리스트를 반환
     *
     * @param scheduleId 조회할 scheduleId
     * @return 정렬된 ScheduleItemResponseDTO 리스트
     */
    @Override
    public List<ScheduleItemResponseDTO> getOrderedItems(Long scheduleId) {
        // 1. scheduleId 를 가지는 첫번째 scheduleItem 조회
        ScheduleItem startItem = findStartItem(scheduleId);

        // 2. LinkedList 를 순회하며 모든 항목을 순서대로 매핑하여 리스트로 반환
        return traverseLinkedList(
                startItem,  // 순회를 시작할 첫번째 ScheduleItem
                ScheduleItemConverter::toScheduleItemResponseDTO,  // 현재 노드를 DTO 로 매핑
                resultList -> false  // 중단 조건 없음
        );
    }

    /**
     * scheduleId를 기반으로 LinkedList 구조를 순회하며
     * 페이지에 필요한 ScheduleItemResponseDTO 리스트를 반환합니다.
     * (리팩토링 필요!!)
     *
     * @param scheduleId 조회할 scheduleId
     * @param pageRequestDTO 페이지 정보
     * @return 페이지 생성시 필요한 item 리스트
     */
    @Override
    public List<ScheduleItemResponseDTO> getPaginatedItems(Long scheduleId, PageRequestDTO pageRequestDTO) {
        // 1. scheduleId 를 가지는 첫번째 scheduleItem 조회
        ScheduleItem startItem = findStartItem(scheduleId);

        // 2. 스킵할 항목 수 계산 (페이지 번호와 페이지 크기 기반)
        int skip = (pageRequestDTO.page()-1) * pageRequestDTO.size();

        // 3. linkedList 를 순회하며 페이지에 해당하는 항목만 매핑하여 리스트로 반환
        return traverseLinkedList(
                startItem,
                // 현재 인덱스가 skip 이후 부터 매핑
                (item, index) -> index >= skip ? ScheduleItemConverter.toScheduleItemResponseDTO(item, index) : null,
                // 결과 리스트 크기가 페이지 크기와 같아지면 순회 중단
                resultList -> resultList.size() >= pageRequestDTO.size()
        );
    }


    /**
     * LinkedList 구조에서 특정 ScheduleItem 의 연결 관계를 업데이트
     * 기존 연결을 끊고 새로운 연결을 설정
     *
     * @param scheduleItem 업데이트할 ScheduleItem
     * @param previousItemId 이전 아이템의 ID (null 가능)
     * @param nextItemId 다음 아이템의 ID (null 가능)
     */
    @Override
    public void updateOrder(ScheduleItem scheduleItem, Long previousItemId, Long nextItemId) {
        // 1. 기존 연결 제거 메서드 호출
        removeCurrentLinks(scheduleItem);

        // 2. 새로운 연결 설정
        if (previousItemId != null && nextItemId != null) {
            // 두 아이템 사이로 이동시
            moveBetweenItems(scheduleItem, previousItemId, nextItemId);
        } else if (previousItemId != null) {
            // 제일 뒤로 이동시
            moveAfterItem(scheduleItem, previousItemId);
        } else if (nextItemId != null) {
            // 제일 앞으로 이동시
            moveBeforeItem(scheduleItem, nextItemId);
        } else {
            throw ScheduleItemException.INVALID_REQUEST.get();
        }
    }

    /**
     * LinkedList 구조에서 특정 ScheduleItem 제거
     * 연결 관계를 재설정하여 아이템 간의 연결유지
     *
     * @param scheduleItem 제거할 ScheduleItem
     */
    @Override
    public void detachItem(ScheduleItem scheduleItem) {
        // 1. 기존 연결 제거 메서드 호출
        removeCurrentLinks(scheduleItem);

        // 2. 현재 노드의 연결 초기화 (안전하게 처리)
        scheduleItem.changePreviousItem(null);
        scheduleItem.changeNextItem(null);
    }

    // 시작아이템 조회
    private ScheduleItem findStartItem(Long scheduleId) {
        return scheduleItemRepository.findFirstByMarkerScheduleScheduleIdAndPreviousItemIsNull(scheduleId)
                .orElseThrow(ScheduleItemException.NOT_FOUND::get);
    }

    // LinkedList 순회 메서드
    private <T> List<T> traverseLinkedList(
            ScheduleItem startItem,  // 순회를 시작할 아이템
            BiFunction<ScheduleItem, Integer, T> mapper,  // 현재 노드와 인덱스를 매핑하여 결과를 생성하는 함수
            Predicate<List<T>> breakCondition  // 결과 리스트를 기반으로 순회를 중단하는 조건
    ) {
        // 결과 리스트 초기화
        List<T> resultList = new ArrayList<>();
        // 순회를 시작할 현재 노드
        ScheduleItem currentItem = startItem;
        // 현재 노드의 인덱스
        int index = 0;

        // LinkedList 순회
        while (currentItem != null) {
            // 중단 조건이 충족되면 순회 종료
            if (breakCondition.test(resultList)) {
                break;
            }
            // 현재 노드를 매핑하여 결과 생성
            T mappedValue = mapper.apply(currentItem, index);
            // 매핑결과가 null 이 아니면 결과 리스트에 추가
            if (mappedValue != null) {
                resultList.add(mappedValue);
            }
            currentItem = currentItem.getNextItem(); // 다음 노드로 이동
            index++; // 인덱스 증가
        }
        return resultList; // 결과 리스트 반환
    }


    // scheduleItem 의 기존 연결 제거
    private void removeCurrentLinks(ScheduleItem scheduleItem) {
        if (scheduleItem.getPreviousItem() != null) {
            scheduleItem.getPreviousItem().changeNextItem(scheduleItem.getNextItem());
        }
        if (scheduleItem.getNextItem() != null) {
            scheduleItem.getNextItem().changePreviousItem(scheduleItem.getPreviousItem());
        }
    }

    // CASE1: 두개의 일정 사이로 이동할 경우
    private void moveBetweenItems(ScheduleItem scheduleItem, Long previousItemId, Long nextItemId) {
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
    }

    // CASE2: 제일 뒤로 이동할 경우
    private void moveAfterItem(ScheduleItem scheduleItem, Long previousItemId) {
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
    }

    // CASE3: 제일 앞으로 이동할 경우
    private void moveBeforeItem(ScheduleItem scheduleItem, Long nextItemId) {
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
    }
}
