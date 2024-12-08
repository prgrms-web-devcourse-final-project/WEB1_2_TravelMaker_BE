package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum ScheduleItemException {
    NOT_FOUND("존재하지 않는 스케쥴아이템입니다.", HttpStatus.NOT_FOUND),
    ITEM_DUPLICATE("해당 마커로 이미 아이템이 존재합니다.", HttpStatus.CONFLICT),
    INVALID_REQUEST("이전 항목 ID와 다음 항목 ID 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST),
    IDS_INVALID("이전 항목 ID 혹은 다음 항목 ID가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

    private final TaskException scheduleItemTaskException;

    ScheduleItemException(String message, HttpStatus status) {
        scheduleItemTaskException = new TaskException(message, status);
    }

    public TaskException get() {
        return scheduleItemTaskException;
    }

}
