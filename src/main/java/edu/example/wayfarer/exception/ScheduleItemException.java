package edu.example.wayfarer.exception;

public enum ScheduleItemException {
    NOT_FOUND("존재하지 않는 스케쥴아이템입니다.", 404),
    ITEM_DUPLICATE("해당 마커로 이미 아이템이 존재합니다.", 409),
    INVALID_REQUEST("이전 항목 ID와 다음 항목 ID 중 하나는 필수입니다.", 400),
    IDS_INVALID("이전 항목 ID 혹은 다음 항목 ID가 유효하지 않습니다.", 400);

    private final ScheduleItemTaskException scheduleItemTaskException;

    ScheduleItemException(String message, int code) {
        scheduleItemTaskException = new ScheduleItemTaskException(message, code);
    }

    public ScheduleItemTaskException get() {
        return scheduleItemTaskException;
    }

}
