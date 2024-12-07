package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum MemberException {
    NOT_FOUND("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    INFO_NOT_FOUND("사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final TaskException memberTaskException;

    MemberException(String message, HttpStatus status) {
        memberTaskException = new TaskException(message, status);
    }

    public TaskException get() {
        return memberTaskException;
    }
}
