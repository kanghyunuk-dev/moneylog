package com.moneylog.backend.exception;

public class DemoLimitExceededException extends RuntimeException {
    public DemoLimitExceededException(String message) {
        super(message);
    }
}
