package edu.example.wayfarer.exception;

import org.springframework.http.HttpStatus;

public enum GeocodingException {
    NULL_RESPONSE("응답이 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    REQUEST_DENIED("요청이 거부되었습니다.", HttpStatus.FORBIDDEN),
    OVER_QUERY_LIMIT("쿼리 한도를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS),
    ZERO_RESULTS("결과를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final TaskException geocodingTaskException;

    GeocodingException(String message, HttpStatus status) {
        geocodingTaskException = new TaskException(message, status);
    }

    public TaskException get() {
        return geocodingTaskException;
    }
}
