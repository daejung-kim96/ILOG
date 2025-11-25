package com.ssafy.auth.domain.model.aggregate;

import com.ssafy.auth.domain.model.enums.Provider;
import com.ssafy.auth.domain.model.vo.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static User oauth2SignUp(String provider, String providerId) {
        return new User(Id.of(0L), Provider.valueOf(provider.toUpperCase()), providerId);
    }

    public static User fromDb(
            Long id,
            Provider provider,
            String providerId) {
        return new User(Id.of(id), provider, providerId);
    }

    private Id id;

    private Provider provider = Provider.GOOGLE;
    private String providerId;
}
