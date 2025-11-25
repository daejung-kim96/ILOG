package com.ssafy.user.presentation.rest.dto.response;

import lombok.Builder;

@Builder
public record UserInfoResponse(
        long id,
        String email,
        String name,
        String picture,
        String createdAt,
        String updatedAt) {
}
