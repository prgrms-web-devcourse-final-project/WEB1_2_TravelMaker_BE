package edu.example.wayfarer.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WebSocketMessageConverter<T> {

    public WebsocketMessage<T> createMessage(String action, T data) {
        return new WebsocketMessage<>(action,data);
    }

    public record WebsocketMessage<T> (String action, T data) {
    }
}
