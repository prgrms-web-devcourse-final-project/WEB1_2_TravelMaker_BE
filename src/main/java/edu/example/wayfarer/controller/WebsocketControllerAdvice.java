package edu.example.wayfarer.controller;

import edu.example.wayfarer.exception.WebSocketException;
import edu.example.wayfarer.exception.WebSocketTaskException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

//메시지 처리 중 발생한 예외를 처리하고, 클라이언트에게 오류 메시지를 전송하는 방식으로 예외를 관리
@ControllerAdvice
public class WebsocketControllerAdvice {

    @MessageExceptionHandler(WebSocketTaskException.class)
    @SendToUser("/queue/errors")
    public String handleWebSocketTaskException(WebSocketTaskException e) {
        return e.getMessage();
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public String handleGenericException(Exception e) {
        return "ERROR: " + e.getMessage();
    }
}
