package com.moneylog.backend.dto.response;

public record ErrorResponse(
        String message,
        String errorCode
) {
    public ErrorResponse(String message) {
        this(message, null);
    }
}