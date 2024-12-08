package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum ScheduleException {
    NOT_FOUND("존재하지 않는 스케쥴입니다.", HttpStatus.NOT_FOUND);

    private final TaskException scheduleTaskException;

    ScheduleException(String message, HttpStatus status) {
        scheduleTaskException = new TaskException(message, status);
    }

    public TaskException get() {
        return scheduleTaskException;
    }
}
