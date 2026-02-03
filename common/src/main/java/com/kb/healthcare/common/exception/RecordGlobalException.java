package com.kb.healthcare.common.exception;

import lombok.Getter;

@Getter
public class RecordGlobalException extends RuntimeException {

    private final String resultCode;
    private final String resultMessage;
    private final Object[] args;

    public RecordGlobalException(String resultCode, String resultMessage) {
        super(resultCode);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.args = null;
    }

    public RecordGlobalException(String resultCode, String resultMessage, Object... args) {
        super(resultCode);
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.args = args;
    }

}