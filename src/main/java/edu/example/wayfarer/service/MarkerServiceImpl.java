package edu.example.wayfarer.service;

import edu.example.wayfarer.converter.MarkerConverter;
import edu.example.wayfarer.dto.marker.MarkerListDTO;
import edu.example.wayfarer.dto.marker.MarkerRequestDTO;
import edu.example.wayfarer.dto.marker.MarkerResponseDTO;
import edu.example.wayfarer.dto.marker.MarkerUpdateDTO;
import edu.example.wayfarer.entity.Marker;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.ScheduleItem;
import edu.example.wayfarer.entity.enums.Color;
import edu.example.wayfarer.exception.*;
import edu.example.wayfarer.manager.ScheduleItemOrderManager;
import edu.example.wayfarer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarkerServiceImpl implements MarkerService {

    private final MarkerRepository markerRepository;
    private final MemberRepository memberRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleItemOrderManager scheduleItemOrderManager;
    private final GeocodingService geocodingService;

    /**
     * 마커 생성 메서드
     * 주어진 MarkerRequestDTO 를 사용하여 Member, Schedule, MemberRoom 를 조회한 뒤
     * 새로운 Marker 를 생성하고 저장한 후, 이를 MarkerResponseDTO 로 변환하여 반환합니다.
     *
     * @param markerRequestDTO Marker 생성 요청 데이터
     * @return MarkerResponseDTO 생성된 Marker 응답 데이터
     */
    @Override
    @Transactional
    public MarkerResponseDTO create(MarkerRequestDTO markerRequestDTO) {
        // 스케쥴의 마커 갯수가 100개 이상일 경우 예외
        Long totalMakerCount = markerRepository.countByScheduleScheduleId(markerRequestDTO.scheduleId());
        if (totalMakerCount >= 100) {
            throw MarkerException.MAX_LIMIT_EXCEEDED.get();
        }

        // 마커 생성을 위한 Member 조회
        // 로그인 구현 이후 수정 예정
        Member member = memberRepository.findById(markerRequestDTO.email())
                .orElseThrow(MemberException.NOT_FOUND::get);

        // 마커 생성을 위한 Schedule 정보 조회
        Schedule schedule = scheduleRepository.findById(markerRequestDTO.scheduleId())
                .orElseThrow(ScheduleException.NOT_FOUND::get);

        // 해당 멤버의 memberRoom.color 조회
        Color color = findColor(member.getEmail(), schedule.getRoom().getRoomId());

        // Marker 저장
        Marker savedMarker = markerRepository.save(
                MarkerConverter.toMarker(
                        markerRequestDTO,
                        member,
                        schedule,
                        color
                )
        );

        // Marker 를 MarkerResponseDTO 로 변환 후 반환
        return MarkerConverter.toMarkerResponseDTO(savedMarker, null);
    }

    /**
     * 마커 조회 메서드
     * markerId로 Marker 조회 후 MarkerResponseDTO 로 변환하여 반환
     *
     * @param markerId 조회할 Marker 의 PK
     * @return MarkerResponseDTO 조회된 Marker 의 응답 데이터
     */
    @Override
    public MarkerResponseDTO read(Long markerId) {
        // markerId 로 Marker 조회
        Marker foundMarker = markerRepository.findById(markerId)
                .orElseThrow(MarkerException.NOT_FOUND::get);

        // itemOrder 값 설정
        Integer itemOrder = Boolean.TRUE.equals(foundMarker.getConfirm())
                ? scheduleItemOrderManager.getIndex(foundMarker.getScheduleItem())
                : null;

        // 조회된 Marker 를 MarkerResponseDTO 로 변환하여 반환
        return MarkerConverter.toMarkerResponseDTO(foundMarker, itemOrder);
    }

    /**
     * 마커 목록 조회 메서드
     * scheduleId 를 기준으로 조회하여 MarkerResponse 리스트로 반환
     *
     * @param scheduleId 조회할 기준
     * @return List<MarkerResponseDTO> 조회된 Marker 들의 응답데이터 리스트
     */
    @Override
//    @Transactional(readOnly = true)
    public List<MarkerResponseDTO> getListBySchedule(Long scheduleId) {
        // scheduleId 로 Marker 리스트 조회
        List<Marker> markers = markerRepository.findByScheduleScheduleId(scheduleId);

        // 조회된 Marker 리스트를 MakerResponseDTO 리스트로 변환하여 반환
        return markers.stream()
                .map(marker -> {
                    // itemOrder 값 설정
                    Integer itemOrder = Boolean.TRUE.equals(marker.getConfirm())
                            ? scheduleItemOrderManager.getIndex(marker.getScheduleItem())
                            : null;
                    // MarkerResponseDTO 생성
                    return MarkerConverter.toMarkerResponseDTO(marker, itemOrder);
                })
                .collect(Collectors.toList());
    }

    /**
     * 전체 마커 목록 조회 메서드
     * roomId 기준으로 모든 Marker 조회
     * 1. roomId 에 해당하는 모든 Schedule 조회
     * 2. 각 Schedule 의 scheduleId 로 Marker 리스트 조회
     * 3. Marker 를 MarkerResponseDTO 로 변환하고 리스트에 담음
     * 4. scheduleId 와 MarkerResponseDTO 리스트로 MarkerListDTO 생성
     *
     * @param roomId Marker 를 조회할 기준
     * @return List<MarkerListDTO> 해당하는 Room 의 모든 Marker 들의 응답데이터 리스트
     */
    @Override
//    @Transactional(readOnly = true)
    public List<MarkerListDTO> getListByRoom(String roomId) {
        // 1. roomId 에 해당하는 모든 Schedule 조회
        return scheduleRepository.findByRoom_RoomId(roomId).stream()
                .map(schedule -> {
                    // 2. 각 Schedule 의 scheduleId 로 Marker 리스트 조회
                    List<MarkerResponseDTO> markerResponseDTOS
                            = markerRepository.findByScheduleScheduleId(schedule.getScheduleId()).stream()
                            // 3. Marker 를 MarkerResponseDTO 로 변환하고 리스트에 담음
                            .map(marker -> {
                                // itemOrder 값 설정
                                Integer itemOrder = Boolean.TRUE.equals(marker.getConfirm())
                                        ? scheduleItemOrderManager.getIndex(marker.getScheduleItem())
                                        : null;
                                // MarkerResponseDTO 생성
                                return MarkerConverter.toMarkerResponseDTO(marker, itemOrder);
                            })
                            .toList();

                    // 4. scheduleId 와 MarkerResponseDTO 리스트로 MarkerListDTO 생성
                    return MarkerConverter.toMarkerListDTO(schedule.getScheduleId(), markerResponseDTOS);
                })
                .toList();
    }

    /**
     * 마커 상태 변경 메서드
     * Marker 의 confirm 을 true 로 변경하는 메서드
     * - true 로 변경 요청시 해당 Marker 의 자식으로 임의의 scheduleItem 생성
     *
     * @param markerUpdateDTO Marker 변경 요청 데이터
     * @return MarkerResponseDTO 수정된 Marker 응답 데이터
     */
    @Override
    @Transactional
    public MarkerResponseDTO update(MarkerUpdateDTO markerUpdateDTO) {
        // 수정할 Marker 조회
        Marker foundMarker = markerRepository.findById(markerUpdateDTO.markerId())
                .orElseThrow(MarkerException.NOT_FOUND::get);

        // 이미 확정 상태일 경우 예외 처리
        if (foundMarker.getConfirm()) {
            throw MarkerException.ALREADY_CONFIRMED.get();
        }

        if (markerUpdateDTO.confirm()) {
            // true 로 변경 요청시 자식 scheduleItem 생성
            saveScheduleItem(foundMarker);
            // Marker 의 confirm 값 변경
            foundMarker.changeConfirm(true);
            // Marker 의 color 를 확정 컬러로 변경
            foundMarker.changeColor(Color.RED);
        }

        Marker savedMarker = markerRepository.save(foundMarker);

        Integer itemOrder = Boolean.TRUE.equals(savedMarker.getConfirm())
                ? scheduleItemOrderManager.getIndex(savedMarker.getScheduleItem())
                : null;
        
        // 수정한 Marker 를 저장 후 MarkerResponseDTO 로 변환하여 반환
        return MarkerConverter.toMarkerResponseDTO(savedMarker, itemOrder);
    }

    /**
     * 마커 삭제 메서드
     * markerId 를 기준으로 마커 삭제
     * @param markerId 삭제할 Marker 의 PK
     */
    @Override
    @Transactional
    public void delete(Long markerId) {
        // 삭제할 Marker 조회
        Marker foundMarker = markerRepository.findById(markerId)
                .orElseThrow(MarkerException.NOT_FOUND::get);

        // 확정마커일 경우 삭제 불가
        if (foundMarker.getConfirm()) {
            throw MarkerException.DELETE_FAIL.get();
        }

        // Marker 삭제
        markerRepository.delete(foundMarker);
    }

    // Marker 의 자식 ScheduleItem 생성 메서드
    @Transactional
    protected void saveScheduleItem(Marker marker) {

        Boolean result = scheduleItemRepository.existsByMarkerMarkerId(marker.getMarkerId());
        if (result) {
            throw ScheduleItemException.ITEM_DUPLICATE.get();
        }

        Long scheduleId = marker.getSchedule().getScheduleId();

        // 스케쥴의 확정 마커 갯수가 50개 이상일 경우 예외
        Long confirmedCount
                = markerRepository.countByScheduleScheduleIdAndConfirmTrue(scheduleId);
        if (confirmedCount >= 50) {
            throw MarkerException.CONFIRMED_LIMIT_EXCEEDED.get();
        }
        // Marker 의 위도, 경도 값으로 주소 조회
        String address = geocodingService.reverseGeocoding(marker.getLat(), marker.getLng());

        // 마지막 ScheduleItem 조회
        ScheduleItem lastItem
                = scheduleItemRepository.findLastByMarkerScheduleScheduleIdAndNextItemIsNull(scheduleId)
                .orElse(null);

        // 새 scheduleItem 생성
        ScheduleItem newItem = ScheduleItem.builder()
                .marker(marker)
                .name(address) // 최초 생성시 주소로 제목 생성
                .content("내용")
                .address(address)
                .previousItem(lastItem)
                .build();

        // linkedList 연결
        if (lastItem != null) {
            lastItem.changeNextItem(newItem);
            scheduleItemRepository.save(lastItem);
        }

        // 새 scheduleItem 저장
        scheduleItemRepository.save(newItem);
    }

    // 마커 작성자의 color 조회 메서드
    private Color findColor(String email, String roomId) {
        // 특정 방 사용자의 color 값 가져오기
        return memberRoomRepository.findByMemberEmailAndRoomRoomId(email, roomId)
                .orElseThrow(MemberRoomException.ROOM_NOT_FOUND::get)
                .getColor();
    }
}
