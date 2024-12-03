package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum RoomException {

    INVALID_DATE("날짜를 제대로 입력해주세요.", HttpStatus.BAD_REQUEST),
    OVER_30DAYS("여행기간설정은 30일까지 가능합니다.", HttpStatus.BAD_REQUEST),
    DOESNT_EXIST("일정이 아직 없습니다.",HttpStatus.NOT_FOUND),
    NOT_FOUND("방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final RoomTaskException roomTaskException;

    RoomException(String message, HttpStatus status){
        roomTaskException = new RoomTaskException(message,status);
    }

    public RoomTaskException get(){
        return roomTaskException;
    }

}
