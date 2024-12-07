package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum RoomException {

    INVALID_DATE("날짜를 제대로 입력해주세요.", HttpStatus.BAD_REQUEST),
    OVER_30DAYS("여행기간설정은 30일까지 가능합니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final TaskException roomTaskException;

    RoomException(String message, HttpStatus status){
        roomTaskException = new TaskException(message,status);
    }

    public TaskException get(){
        return roomTaskException;
    }

}
