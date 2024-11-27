package edu.example.wayfarer.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class WebSocketMessageConverter<T> {

    public WebsocketMessage<T> createMessage(String action, T data) {
        return new WebsocketMessage<>(action,data);
    }


    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class WebsocketMessage<T> {
        private final String action;
        private final T data;
    }
}
