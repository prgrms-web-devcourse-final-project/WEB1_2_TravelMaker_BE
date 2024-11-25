package edu.example.wayfarer.service;

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
import edu.example.wayfarer.repository.MemberRepository;
import edu.example.wayfarer.repository.MemberRoomRepository;
import edu.example.wayfarer.repository.RoomRepository;
import edu.example.wayfarer.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ModelMapper modelMapper;

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
        // 여행 끝 날짜가 더 나중이 맞는지 확인
        if(roomRequestDTO.getStartDate().isAfter(roomRequestDTO.getEndDate())) {
            throw RoomException.INVALID_DATE.get();
        }

        //여행 기간이 30일 이내인지 확인
        long daysBetween = ChronoUnit.DAYS.between(roomRequestDTO.getStartDate(), roomRequestDTO.getEndDate()) + 1;
        if(daysBetween > 30) {
            throw RoomException.OVER_30DAYS.get();
        }

        // room 저장
        Room room = modelMapper.map(roomRequestDTO, Room.class);
        System.out.println("Host email: " + room.getHostEmail());

        // roomId 생성하고 중복 확인
        String roomId;
        do{
            room.generateRoomId();
            roomId = room.getRoomId();
        }while (roomRepository.existsById(roomId));
        Room savedRoom = roomRepository.save(room);
        System.out.println("Saved Room ID: " + savedRoom.getRoomId());
        System.out.println("Host email: " + room.getHostEmail());

        //memberRoom 저장
        // 방장을 찾는다
        Member foundMember = memberRepository.findById(room.getHostEmail()).orElseThrow();
        // Color enum을 배열화
        Color[] colors = Color.values();
        // memberRoom을 build
        MemberRoom memberRoom = MemberRoom.builder()
                .member(foundMember)
                .room(savedRoom)
                .color(colors[1]).build();
        memberRoomRepository.save(memberRoom);

        // schedule 저장
        List<Schedule> schedules = new ArrayList<>();

        Days[] days = Days.values();
        for(int i = 0; i < daysBetween; i++) {
            for (PlanType planType : PlanType.values()){
                schedules.add(Schedule.builder()
                        .room(savedRoom)
                        .planType(planType)
                        .date(days[i])
                        .build()
                );
            }
        }
        for (Schedule schedule : schedules) {
            System.out.println("Schedule: date=" + schedule.getDate() +
                    ", planType=" + schedule.getPlanType() +
                    ", roomId=" + (schedule.getRoom() != null ? schedule.getRoom().getRoomId() : "null"));
            // 이 시스템 아웃은 그냥 확인용으로 찍어본 것입니다
            scheduleRepository.save(schedule);
        }

        return new RoomResponseDTO(savedRoom);
    }

    @Override
    public RoomResponseDTO read(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new NoSuchElementException("해당 방이 존재하지 않습니다."));
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
        Room room = roomRepository.findById(roomUpdateDTO.getRoomId())
                .orElseThrow(()-> new NoSuchElementException("해당 방이 존재하지 않습니다."));

        room.changeCountry(roomUpdateDTO.getCountry());
        room.changeTitle(roomUpdateDTO.getTitle());

        long oldSession = ChronoUnit.DAYS.between(room.getStartDate(), room.getEndDate())+1;
        long newSession = ChronoUnit.DAYS.between(roomUpdateDTO.getStartDate(), roomUpdateDTO.getEndDate())+1;

        room.changeStartDate(roomUpdateDTO.getStartDate());
        room.changeEndDate(roomUpdateDTO.getEndDate());

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
    public void delete(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("삭제할 방이 존재하지 않습니다."));

        scheduleRepository.deleteByRoomId(roomId);
        memberRoomRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);
    }

}
