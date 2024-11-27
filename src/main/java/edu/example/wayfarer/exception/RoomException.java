package edu.example.wayfarer.exception;

public enum RoomException {

    INVALID_DATE("날짜를 제대로 입력해주세요."),
    OVER_30DAYS("여행기간설정은 30일까지 가능합니다."),
    DOESNT_EXIST("일정이 아직 없습니다.");

    private RoomTaskException roomTaskException;

    RoomException(String message){
        roomTaskException = new RoomTaskException(message);
    }

    public RoomTaskException get(){
        return roomTaskException;
    }

}
