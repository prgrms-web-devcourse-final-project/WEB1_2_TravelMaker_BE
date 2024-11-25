package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberRoomTaskException extends RuntimeException {
    private String message;

}
