package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ScheduleTaskException extends RuntimeException {

    private HttpStatus status;

    public ScheduleTaskException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
