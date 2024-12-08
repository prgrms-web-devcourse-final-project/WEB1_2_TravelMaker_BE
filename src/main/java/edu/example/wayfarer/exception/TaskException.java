package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class TaskException extends RuntimeException {

    private HttpStatus status;

    public TaskException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}