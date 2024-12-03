package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum ChatMessageException {
    MESSAGE_NOT_FOUND("해당 메세지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("글의 작성자만 수행할 수 있습니다.", HttpStatus.UNAUTHORIZED);

    private final MemberTaskException memberTaskException;

    ChatMessageException(String message, HttpStatus status) {
        memberTaskException = new MemberTaskException(message, status);
    }

    public MemberTaskException get(){
        return memberTaskException;
    }
}
