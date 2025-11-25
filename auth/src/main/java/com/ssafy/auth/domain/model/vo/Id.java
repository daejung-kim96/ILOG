package com.ssafy.auth.domain.model.vo;

import lombok.Builder;

@Builder
public record Id(long value) {
    public static Id of(long value) {
        return new Id(value);
    }
}
