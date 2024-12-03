package edu.example.wayfarer.controller.advice;

import edu.example.wayfarer.exception.MarkerTaskException;
import edu.example.wayfarer.exception.ScheduleItemTaskException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class APIControllerAdvice {

    @ExceptionHandler(MarkerTaskException.class)
    public ResponseEntity<?> handleMarkerTaskException(MarkerTaskException e) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error message", e.getMessage());
        errorResponse.put("status", e.getStatus());
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(ScheduleItemTaskException.class)
    public ResponseEntity<?> handleScheduleItemTaskException(ScheduleItemTaskException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error message", e.getMessage());
        errorResponse.put("status", e.getStatus());
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }
}
