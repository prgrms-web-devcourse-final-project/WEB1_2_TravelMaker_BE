package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum MemberRoomException {

    INVALID_ROOMCODE("ROOMCODE가 맞지 않습니다.", HttpStatus.BAD_REQUEST),
    OVER_CAPACITY("10명 정원을 초과하였습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATED_MEMBER("이미 들어와 있는 사용자입니다.", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND("방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),;


    private final MemberRoomTaskException memberRoomTaskException;

    MemberRoomException(String message, HttpStatus status){
        memberRoomTaskException = new MemberRoomTaskException(message, status);
    }

    public MemberRoomTaskException get(){
        return memberRoomTaskException;
    }

}
