package edu.example.wayfarer.exception;

public enum MarkerException {

    NOT_FOUND("존재하지 않는 마커입니다.", 404),
    DELETE_FAIL("확정마커는 삭제할 수 없습니다.", 400),
    MAX_LIMIT_EXCEEDED("마커 생성 제한 갯수를 초과했습니다.", 400),
    CONFIRMED_LIMIT_EXCEEDED("마커 확정 제한 갯수를 초과했습니다.", 400);

    private final MarkerTaskException markerTaskException;

    MarkerException(String message, int code) {
        markerTaskException = new MarkerTaskException(message, code);
    }

    public MarkerTaskException get() {
        return markerTaskException;
    }
}
