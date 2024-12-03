package edu.example.wayfarer.apiPayload.exception.handler;

import edu.example.wayfarer.apiPayload.code.BaseErrorCode;
import edu.example.wayfarer.apiPayload.exception.GeneralException;

public class AuthHandler extends GeneralException {

    public AuthHandler(BaseErrorCode code) {
        super(code);
    }
}