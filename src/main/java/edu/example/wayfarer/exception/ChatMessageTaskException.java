package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ChatMessageTaskException extends RuntimeException {
    private HttpStatus status;

    public ChatMessageTaskException(String message, HttpStatus status){
        super(message);
        this.status = status;
    }
}
