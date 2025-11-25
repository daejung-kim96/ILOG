package com.ssafy.user.domain.model.aggregate;

import java.time.LocalDateTime;

import com.ssafy.user.domain.model.vo.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static User createUser(Long id, String email, String picture, String name) {
        return new User(Id.of(id), email, picture, name, null, null);
    }

    public static User fromDb(
            Long id,
            String email,
            String picture,
            String name,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new User(Id.of(id), email, picture, name, createdAt, updatedAt);
    }

    private Id id;

    private String email;
    private String picture;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
