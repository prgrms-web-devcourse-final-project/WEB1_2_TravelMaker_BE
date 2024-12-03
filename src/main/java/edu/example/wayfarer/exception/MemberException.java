package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum MemberException {
    NOT_FOUND("존재하지 않는 맴버입니다.", HttpStatus.NOT_FOUND);

    private final MemberTaskException memberTaskException;

    MemberException(String message, HttpStatus status) {
        memberTaskException = new MemberTaskException(message, status);
    }

    public MemberTaskException get() {
        return memberTaskException;
    }
}
