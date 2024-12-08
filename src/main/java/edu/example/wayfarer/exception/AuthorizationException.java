package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum AuthorizationException {
    NO_TOKEN("인증 정보가 없습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("인증된 사용자 정보를 확인할 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("권한이 없습니다.", HttpStatus.UNAUTHORIZED),;

    private final TaskException taskException;

    AuthorizationException(String message, HttpStatus httpStatus) {
        taskException = new TaskException(message, httpStatus);
    }

    public TaskException get(){
        return taskException;
    }

}
