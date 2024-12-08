package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum MarkerException {

    NOT_FOUND("존재하지 않는 마커입니다.", HttpStatus.NOT_FOUND),
    DELETE_FAIL("확정마커는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    MAX_LIMIT_EXCEEDED("마커 생성 제한 갯수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    CONFIRMED_LIMIT_EXCEEDED("마커 확정 제한 갯수를 초과했습니다.", HttpStatus.BAD_REQUEST),
    ALREADY_CONFIRMED("이미 확정 상태인 마커입니다.", HttpStatus.BAD_REQUEST);

    private final TaskException markerTaskException;

    MarkerException(String message, HttpStatus status) {
        markerTaskException = new TaskException(message, status);
    }

    public TaskException get() {
        return markerTaskException;
    }
}
