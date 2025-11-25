package com.ssafy.auth.application.oauth2;

import com.ssafy.auth.application.facade.AuthFacade;
import com.ssafy.auth.domain.model.aggregate.User;
import com.ssafy.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
// DefaultOidcUserService를 import 합니다.
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// OidcUser를 import 합니다.
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
// 1. DefaultOidcUserService를 상속받도록 변경합니다.
public class CustomOAuth2UserService extends OidcUserService {

    private final AuthFacade authFacade;
    private final UserRepository userRepository;

    // 2. loadUser 메서드의 시그니처가 OidcUserRequest로 변경됩니다.
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // 3. 부모 클래스(OidcUserService)의 loadUser를 호출하여 OidcUser를 가져옵니다.
        OidcUser oidcUser = super.loadUser(userRequest);

        // 4. OAuth2Attribute 파싱 로직은 그대로 사용합니다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuth2Attribute oAuth2Attribute = OAuth2Attribute.of(registrationId, userNameAttributeName,
                oidcUser.getAttributes()); // oidcUser의 attributes 사용

        String email = oAuth2Attribute.getEmail();
        String provider = oAuth2Attribute.getProvider();
        String providerId = oAuth2Attribute.getProviderId();
        String name = oAuth2Attribute.getName();
        String picture = oAuth2Attribute.getPicture();

        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = authFacade.createUser(email, provider, providerId, name, picture);
        }

        // 5. 수정된 PrincipalDetails 생성자를 사용합니다.
        // oidcUser.getAttributes()와 oidcUser.getIdToken()을 넘겨줍니다.
        return new PrincipalDetails(user, oidcUser.getAttributes(), userNameAttributeName, oidcUser.getIdToken());
    }
}