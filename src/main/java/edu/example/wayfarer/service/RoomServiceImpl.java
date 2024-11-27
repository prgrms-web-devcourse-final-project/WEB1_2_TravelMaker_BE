package edu.example.wayfarer.service;

import edu.example.wayfarer.apiPayload.exception.AuthorizationException;
import edu.example.wayfarer.auth.util.KakaoUtil;
import edu.example.wayfarer.auth.util.SecurityUtil;
import edu.example.wayfarer.dto.room.RoomRequestDTO;
import edu.example.wayfarer.dto.room.RoomResponseDTO;
import edu.example.wayfarer.dto.room.RoomUpdateDTO;
import edu.example.wayfarer.entity.Member;
import edu.example.wayfarer.entity.MemberRoom;
import edu.example.wayfarer.entity.Room;
import edu.example.wayfarer.entity.Schedule;
import edu.example.wayfarer.entity.enums.Color;
import edu.example.wayfarer.entity.enums.Days;
import edu.example.wayfarer.entity.enums.PlanType;
import edu.example.wayfarer.exception.RoomException;
import edu.example.wayfarer.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MemberRoomRepository memberRoomRepository;
    private final ScheduleRepository scheduleRepository;

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
    public RoomResponseDTO create(RoomRequestDTO roomRequestDTO) {
        // 날짜 유효성 검사
        validateDates(roomRequestDTO);


        Room room = Room.builder()
                .title(roomRequestDTO.title())
                .country(roomRequestDTO.country())
                .startDate(roomRequestDTO.startDate())
                .endDate(roomRequestDTO.endDate())
                .hostEmail(roomRequestDTO.email())
                .memberRooms(new ArrayList<>())
                .build();

        // 랜덤 roomId와 roomCode 생성
        generateRoomIdAndCode(room);
        // url 생성
        String url = generateRoomUrl(room.getRoomId());
        room.setUrl(url);
        // 방 저장
        Room savedRoom = roomRepository.save(room);

        //memberRoom 저장
        // currentUser로 지정 나중에
        Member foundMember = memberRepository.findById(room.getHostEmail()).orElseThrow();
        // Color enum을 배열화
        Color[] colors = Color.values();
        // memberRoom을 build
        MemberRoom memberRoom = MemberRoom.builder()
                .member(foundMember)
                .room(savedRoom)
                .color(colors[1]).build();
        savedRoom.getMemberRooms().add(memberRoom);
        memberRoomRepository.save(memberRoom);

        // schedule 저장
        saveSchedules(savedRoom, roomRequestDTO.startDate(), roomRequestDTO.endDate());

        return new RoomResponseDTO(savedRoom);
    }

    @Override
    public RoomResponseDTO read(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new NoSuchElementException("해당 방이 존재하지 않습니다."));

        List<MemberRoom> members = memberRoomRepository.findAllByRoom_RoomId(roomId);
        room.setMemberRooms(members);
        return new RoomResponseDTO(room);
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
    public RoomResponseDTO update(RoomUpdateDTO roomUpdateDTO) {
        Room room = roomRepository.findById(roomUpdateDTO.roomId())
                .orElseThrow(()-> new NoSuchElementException("해당 방이 존재하지 않습니다."));

        // 로그인한 사용자가 해당 방의 방장이 맞는지 아닌지 확인
        if(!roomUpdateDTO.member().getEmail().equals(room.getHostEmail())){
            throw new AuthorizationException("권한이 없습니다.");
        }

        room.changeCountry(roomUpdateDTO.country());
        room.changeTitle(roomUpdateDTO.title());

        long oldSession = ChronoUnit.DAYS.between(room.getStartDate(), room.getEndDate())+1;
        long newSession = ChronoUnit.DAYS.between(roomUpdateDTO.startDate(), roomUpdateDTO.endDate())+1;

        room.changeStartDate(roomUpdateDTO.startDate());
        room.changeEndDate(roomUpdateDTO.endDate());

        if(newSession > oldSession){
            for(long i = oldSession + 1; i <= newSession; i++){
                System.out.println("starting DAY"+i);
                String dayValue = "DAY" + i;
                Days day = Days.valueOf(dayValue);

                for (PlanType planType : PlanType.values()){
                    Schedule newSchedule = Schedule.builder()
                            .room(room)
                            .date(day)
                            .planType(planType)
                            .build();
                    scheduleRepository.save(newSchedule);
                }
            }
        }else if(newSession < oldSession){
            for(long i = newSession + 1; i <= oldSession; i++){
                String dayValue = "DAY" + i;
                Days day = Days.valueOf(dayValue);

                List<Schedule> schedulesToDelete = scheduleRepository.findByRoomAndDate(room, day);
                scheduleRepository.deleteAll(schedulesToDelete);
            }
        }
        return new RoomResponseDTO(roomRepository.save(room));
    }

    @Override
    public void delete(Member member, String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("삭제할 방이 존재하지 않습니다."));

        // 로그인한 사용자와 room.HostEmail이 맞지 않으면 오류처리 : 방장만 삭제 가능합니다
        if(!member.getEmail().equals(room.getHostEmail())){
            throw new AuthorizationException("권한이 없습니다.");
        }
        scheduleRepository.deleteByRoomId(roomId);
        memberRoomRepository.deleteByRoomId(roomId);
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

    private String generateRoomUrl(String roomId){
        return "https://wayfarer.com/rooms/" + roomId;
    }

    private void saveSchedules(Room room, LocalDate startDate, LocalDate endDate) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<Schedule> schedules = new ArrayList<>();

        Days[] days = Days.values();
        for(int i = 0; i < daysBetween; i++) {
            for (PlanType planType : PlanType.values()){
                schedules.add(Schedule.builder()
                        .room(room)
                        .planType(planType)
                        .date(days[i])
                        .build()
                );
            }
        }
        scheduleRepository.saveAll(schedules);
    }

}
