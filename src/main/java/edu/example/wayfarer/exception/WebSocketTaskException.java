package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebSocketTaskException extends RuntimeException {
  private final int code;

  public WebSocketTaskException(WebSocketException exception) {
    super(exception.getMessage());
    this.code = exception.getCode();
  }
}
