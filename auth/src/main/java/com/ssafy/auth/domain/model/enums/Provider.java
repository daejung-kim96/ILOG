package com.ssafy.auth.domain.model.enums;

import java.util.Locale;

public enum Provider {
    GOOGLE, KAKAO, NAVER;

    public static Provider fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Provider value cannot be null");
        }

        return Provider.valueOf(value.toUpperCase(Locale.ENGLISH));
    }
}
