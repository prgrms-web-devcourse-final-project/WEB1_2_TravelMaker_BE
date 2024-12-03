package edu.example.wayfarer.apiPayload.exception.handler;

import edu.example.wayfarer.apiPayload.code.BaseErrorCode;
import edu.example.wayfarer.apiPayload.exception.GeneralException;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}
