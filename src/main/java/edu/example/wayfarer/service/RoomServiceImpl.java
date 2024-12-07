package edu.example.wayfarer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.wayfarer.exception.AuthorizationException;
import edu.example.wayfarer.converter.MemberRoomConverter;
import edu.example.wayfarer.converter.RoomConverter;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.entity.*;
import edu.example.wayfarer.entity.enums.Color;
//import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;
import edu.example.wayfarer.exception.RoomException;
import edu.example.wayfarer.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    @Autowired
    @Qualifier("jsonRedisTemplate")
    private RedisTemplate<String, Object> jsonRedisTemplate;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final ScheduleRepository scheduleRepository;
    private final ObjectMapper objectMapper;


    /*
    create 설명
    1. roomRequestDTO의 endDate가 startDate보다 나중이 맞는지 확인 (아니라면 INVALID_DATE 예외)
    2. 여행 기간이 30일 이내가 맞는지 확인 (아니라면 OVER_30DAYS 예외)
    3. 데이터베이스에 ROOM 생성
        - modelMapper를 활용한 엔티티로 변환
        - 랜덤 8자 문자열 roomId 생성 + 중복 확인
    4. 데이터베이스에 ROOMMEMBER 생성
        - 맨 처음 만들어질 때 방장은 그냥 COLOR(1) 고정
    5. 데이터베이스에 SCHEDULE 생성
        - 여행 기간 + PLAN ABC 중첩 반복하여 INSERT
     */
    @Override
    @Transactional
    public RoomResponseDTO create(RoomRequestDTO roomRequestDTO, String email) {
        // 날짜 유효성 검사
        validateDates(roomRequestDTO);
        Room room = RoomConverter.toRoom(roomRequestDTO);
        room.setHostEmail(email);

        // 랜덤 roomId와 roomCode 생성
        generateRoomIdAndCode(room);
        // 방 저장
        Room savedRoom = roomRepository.save(room);

        //memberRoom 저장
        Member foundMember = memberRepository.findById(room.getHostEmail()).orElseThrow();
        // Color enum을 배열화
        Color[] colors = Color.values();
        MemberRoom memberRoom = MemberRoomConverter.toMemberRoom(savedRoom, foundMember, colors[1]);
        savedRoom.getMemberRooms().add(memberRoom);
        memberRoomRepository.save(memberRoom);

        //Redis에 memberRoom 캐시 추가
        Map<String, Object> memberInfo = new LinkedHashMap<>();
        memberInfo.put("nickname", foundMember.getNickname());
        memberInfo.put("profileImage", foundMember.getProfileImage());

        try {
            // Field, Map을 JSON 문자열로 변환
            String jsonMemberInfo = objectMapper.writeValueAsString(memberInfo);
            jsonRedisTemplate.opsForHash().put("Member:" + room.getRoomId(), email, jsonMemberInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // schedule 저장
        saveSchedules(savedRoom, roomRequestDTO.startDate(), roomRequestDTO.endDate());

        return RoomConverter.toRoomResponseDTO(savedRoom);
    }

    @Override
    public RoomResponseDTO read(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomException.NOT_FOUND::get);

        List<MemberRoom> members = memberRoomRepository.findAllByRoomRoomId(roomId);
        room.setMemberRooms(members);
        return RoomConverter.toRoomResponseDTO(room);
    }

    /*
    update 설명
    1. 해당 방을 찾습니다.
    2. country, title, 날짜를 모두 바꿉니다.
    3. 전 여행기간인 oldSession과 새로운 여행기간인 newSession을 선언하여 기간을 비교합니다.
    4. 여행이 더 길어졌다면, 해당 기간만큼의 schedule 수를 늘려줍니다.
    5. 여행이 짧아졌다면, 기존에 있던 마지막 날들을 삭제합니다.
     */
    @Override
    public RoomResponseDTO update(RoomUpdateDTO roomUpdateDTO, String email) {
        Room room = roomRepository.findById(roomUpdateDTO.roomId())
                .orElseThrow(RoomException.NOT_FOUND::get);

        // 권한 확인
        verifyHost(email, room.getHostEmail());

        // 방 정보 업데이트
        updateRoomDetails(room, roomUpdateDTO);

        // 스케줄 정보 업데이트
        updateSchedules(room, roomUpdateDTO);
        return RoomConverter.toRoomResponseDTO(roomRepository.save(room));
    }

    // 방 삭제 메서드
    @Override
    public void delete(Member member, String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomException.NOT_FOUND::get);

        verifyHost(member.getEmail(), room.getHostEmail());

        jsonRedisTemplate.delete("ChatMessage:" + roomId);
        jsonRedisTemplate.delete("Member:" + roomId);

        roomRepository.delete(room);
    }

    private void validateDates(RoomRequestDTO roomRequestDTO) {
        if(roomRequestDTO.startDate().isAfter(roomRequestDTO.endDate())){
            throw RoomException.INVALID_DATE.get();
        }

        long daysBetween = ChronoUnit.DAYS.between(roomRequestDTO.startDate(), roomRequestDTO.endDate()) + 1;
        if(daysBetween > 30){
            throw RoomException.OVER_30DAYS.get();
        }
    }

    private void generateRoomIdAndCode(Room room){
        String roomId;
        String roomCode;
        do {
            room.generateRoomIdAndRoomCode();
            roomId = room.getRoomId();
            roomCode = room.getRoomCode();
        } while (roomRepository.existsById(roomId));

        room.setRoomId(roomId);
        room.setRoomCode(roomCode);
    }


    private void saveSchedules(Room room, LocalDate startDate, LocalDate endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<Schedule> schedules = new ArrayList<>();

        for(int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            for (PlanType planType : PlanType.values()){
                schedules.add(Schedule.builder()
                        .room(room)
                        .planType(planType)
                        .actualDate(currentDate)
                        .build()
                );
            }

        }
        scheduleRepository.saveAll(schedules);
    }

    private void verifyHost(String requestEmail, String hostEmail){
        if(!requestEmail.equals(hostEmail)){
            throw AuthorizationException.UNAUTHORIZED.get();
        }
    }

    private void updateRoomDetails(Room room, RoomUpdateDTO roomUpdateDTO) {
        room.changeTitle(roomUpdateDTO.title());
        room.changeCountry(roomUpdateDTO.country());
        room.changeStartDate(roomUpdateDTO.startDate());
        room.changeEndDate(roomUpdateDTO.endDate());
    }

    private void updateSchedules(Room room, RoomUpdateDTO roomUpdateDTO){
        long oldSession = ChronoUnit.DAYS.between(room.getStartDate(), room.getEndDate())+1;
        long newSession = ChronoUnit.DAYS.between(roomUpdateDTO.startDate(), roomUpdateDTO.endDate())+1;

        LocalDate newStartDate = roomUpdateDTO.startDate(); // 새로운 여행 시작일

        List<Schedule> allSchedules = scheduleRepository.findByRoom_RoomId(roomUpdateDTO.roomId());
        updateExistingSchedule(allSchedules, newStartDate);

        if(newSession > oldSession) {
            addNewSchedules(room, newStartDate, oldSession, newSession);
        } else if (newSession < oldSession) {
            deleteExtraSchedule(room, newStartDate, newSession, oldSession);
        }
    }

    private void updateExistingSchedule(List<Schedule> schedules, LocalDate newStartDate){
        for(int i =0; i<schedules.size(); i++){
            Schedule schedule = schedules.get(i);
            LocalDate updatedDate = newStartDate.plusDays(i / PlanType.values().length);
            schedule.changeActualDate(updatedDate);
        }
        scheduleRepository.saveAll(schedules);
    }

    private void addNewSchedules(Room room, LocalDate newStartDate, long oldSession, long newSession){
        for(long i = oldSession + 1; i <= newSession; i++){

            LocalDate actualDate = newStartDate.plusDays(i-1);  // 새로운 시작 날짜 기준으로 actualDate 계산

            for (PlanType planType : PlanType.values()){
                Schedule newSchedule = Schedule.builder()
                        .room(room)
                        .actualDate(actualDate)
                        .planType(planType)
                        .build();
                scheduleRepository.save(newSchedule);
            }
        }
    }

    private void deleteExtraSchedule(Room room, LocalDate newStartDate, long newSession, long oldSession){
        for(long i = newSession + 1; i <= oldSession; i++){

            LocalDate actualDate = newStartDate.plusDays(i-1);
            List<Schedule> schedulesToDelete = scheduleRepository.findByRoomAndActualDate(room, actualDate);
            scheduleRepository.deleteAll(schedulesToDelete);
        }
    }

}
