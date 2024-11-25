package edu.example.wayfarer.exception;

public enum MemberRoomException {

    INVALID_ROOMCODE("ROOMCODE가 맞지 않습니다."),
    OVER_CAPACITY("10명 정원을 초과하였습니다."),
    DUPLICATED_MEMBER("이미 들어와 있는 사용자입니다.");

    private MemberRoomTaskException memberRoomTaskException;

    MemberRoomException(String message){
        memberRoomTaskException = new MemberRoomTaskException(message);
    }

    public MemberRoomTaskException get(){
        return memberRoomTaskException;
    }

}
