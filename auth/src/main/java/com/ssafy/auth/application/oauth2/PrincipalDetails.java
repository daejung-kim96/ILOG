package com.ssafy.auth.application.oauth2;

import com.ssafy.auth.domain.model.aggregate.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class PrincipalDetails implements OidcUser {

    private User user;
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private OidcIdToken idToken;

    // 생성자 변경: idToken을 받도록 수정
    public PrincipalDetails(User user, Map<String, Object> attributes, String nameAttributeKey, OidcIdToken idToken) {
        this.user = user;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.idToken = idToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        // nameAttributeKey를 사용하거나, user의 고유 ID를 사용합니다.
        // 예: return attributes.get(this.nameAttributeKey).toString();
        return user.getProviderId(); // 기존 로직 유지
    }

    @Override
    public Map<String, Object> getClaims() {
        // attributes와 동일한 값을 반환해도 무방합니다.
        return this.attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        // attributes로 OidcUserInfo 객체를 생성하여 반환합니다.
        return new OidcUserInfo(this.attributes);
    }

    @Override
    public OidcIdToken getIdToken() {
        // 생성자에서 받은 idToken을 반환합니다.
        return this.idToken;
    }
}