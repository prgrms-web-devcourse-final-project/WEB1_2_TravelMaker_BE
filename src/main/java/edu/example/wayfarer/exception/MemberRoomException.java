package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum MemberRoomException {

    INVALID_ROOMCODE("ROOMCODE가 맞지 않습니다.", HttpStatus.BAD_REQUEST),
    OVER_CAPACITY("10명 정원을 초과하였습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_MEMBER("이미 들어와 있는 사용자입니다.", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND("방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    HOST_NOT_FOUND("다음 방장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LEFT_MEMBER("이미 퇴장한 사용자입니다.", HttpStatus.BAD_REQUEST);


    private final TaskException memberRoomTaskException;

    MemberRoomException(String message, HttpStatus status){
        memberRoomTaskException = new TaskException(message, status);
    }

    public TaskException get(){
        return memberRoomTaskException;
    }

    // 예외 던지기
    public void throwException() {
        throw new TaskException(memberRoomTaskException.getMessage(), memberRoomTaskException.getStatus());
    }

}
