package edu.example.wayfarer.exception;

public enum WebSocketException {

    INVALID_TOKEN("Invalid token", 401),
    INVALID_MESSAGE_FORMAT("Invalid message format", 1002), // 1002: Protocol Error
    INVALID_ACTION("Invalid action", 1003),
    INVALID_EMAIL("Invalid email", 400);

    private final String message;
    private final int code;

    WebSocketException(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }
    public int getCode() {
        return code;
    }
}
