package com.ssafy.user.presentation.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SignupRequest(
        @NotNull String email,
        @NotNull String password) {
}
