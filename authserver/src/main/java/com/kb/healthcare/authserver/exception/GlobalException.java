package com.kb.healthcare.authserver.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final String resultCode;
    private final String resultMessage;
    private final Object[] args;

    public GlobalException(String resultCode, String resultMessage) {
        super(resultCode);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.args = null;
    }

    public GlobalException(String resultCode, String resultMessage, Object... args) {
        super(resultCode);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.args = args;
    }

}
