package edu.example.wayfarer.apiPayload.exception.handler;

import edu.example.wayfarer.apiPayload.code.BaseErrorCode;
import edu.example.wayfarer.apiPayload.exception.GeneralException;

public class TestHandler extends GeneralException {
    public TestHandler(BaseErrorCode code) {
        super(code);
    }
}
