package edu.example.wayfarer.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MarkerTaskException extends RuntimeException {

    private String message;
    private int code;
}
