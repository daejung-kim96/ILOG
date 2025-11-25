package com.ssafy.user.application.query.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoModel {
    private Long id;
    private String name;
    private String email;
    private String picture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserInfoModel fromDb(Long id, String email, String picture, String name,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new UserInfoModel(id, name, email, picture, createdAt, updatedAt);
    }

}
