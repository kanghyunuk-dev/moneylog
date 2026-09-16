package com.moneylog.backend.exception;

public class GoalAlreadyDoneException extends RuntimeException {
    public GoalAlreadyDoneException(String message) {
        super(message);
    }
}
