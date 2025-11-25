package com.ssafy.auth.common.response;

public record ValidationError(
        String field,
        String message,
        Object rejectedValue) {

    public static ValidationError of(String field, String message, Object rejectedValue) {
        return new ValidationError(field, message, rejectedValue);
    }
}