package edu.example.wayfarer.exception;

public enum MarkerException {

    NOT_FOUND("존재하지 않는 마커입니다.", 404);

    private final MarkerTaskException markerTaskException;

    MarkerException(String message, int code) {
        markerTaskException = new MarkerTaskException(message, code);
    }

    public MarkerTaskException get() {
        return markerTaskException;
    }
}
