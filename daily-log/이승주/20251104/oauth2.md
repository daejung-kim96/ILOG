oauth2Login() 함수(정확히는 DSL 설정 메서드)가 요청을 가로채는 방식은, 우리가 흔히 생각하는 @Controller 방식이 아니라 Spring Security의 핵심인 Filter (필터) 체인을 이용하기 때문입니다.

http.oauth2Login() 코드를 추가하는 순간, Spring Security는 HTTP 요청을 처리하는 SecurityFilterChain에 OAuth2 로그인 전용 필터 두 개를 자동으로 등록합니다.

1. OAuth2AuthorizationRequestRedirectFilter
   "로그인 시작"을 가로채는 필터

이 필터는 OAuth2 로그인을 시작하는 요청을 감시합니다.

감시 경로: GET /oauth2/authorization/{providerId} (예: /oauth2/authorization/google)

동작:

사용자가 GET /oauth2/authorization/google을 요청하면 이 필터가 요청을 가로챕니다.

providerId (여기서는 "google")를 확인하고, application.yml에 설정된 ClientRegistration (client-id, scope 등)을 찾습니다.

CSRF 방어를 위한 state 값을 생성합니다.

Google로 보내야 할 최종 인증 URL을 만듭니다. (모든 파라미터 포함)

브라우저에게 이 URL로 가라는 HTTP 302 Redirect 응답을 보냅니다.

이 필터는 Controller까지 요청이 도달하기 전에 모든 일을 처리하고 응답을 끝냅니다.

2. OAuth2LoginAuthenticationFilter
   "로그인 콜백"을 가로채는 필터 (핵심)

이 필터는 Google/Kakao 등에서 로그인을 성공한 뒤, 인증 코드를 가지고 돌아오는 콜백(Callback) 요청을 감시합니다. 이 필터가 바로 사용자님이 설정한 3개의 컴포넌트(userService, successHandler, failureHandler)를 사용하는 주체입니다.

감시 경로: GET /login/oauth2/code/{providerId} (예: /login/oauth2/code/google)

동작:

Google이 사용자를 GET /login/oauth2/code/google?code=...&state=...로 리디렉션시키면, 이 필터가 요청을 가로챕니다.

요청에서 code와 state 파라미터를 추출합니다.

(1단계)에서 저장했던 state 값과 (2단계)에서 받은 state 값이 일치하는지 검사합니다. (CSRF 방어)

(내부 통신 1) 백그라운드에서 Google 토큰 서버에 code와 client-secret 등을 보내 Access Token을 받아옵니다.

(내부 통신 2) 받아온 Access Token으로 Google 리소스 서버(User Info API)에 **사용자 정보(profile, email 등)**를 요청합니다.

➡️ userService 호출:

Google로부터 사용자 정보를 성공적으로 받아오면, 이 필터는 사용자님이 설정한 customOAuth2UserService의 loadUser() 메서드를 호출합니다.

loadUser()가 DB 조회/저장을 마치고 PrincipalDetails 객체를 반환합니다.

필터는 이 PrincipalDetails를 기반으로 Authentication 객체를 생성하여 SecurityContextHolder에 저장합니다. (이 순간, 서버에서 "로그인 상태"가 됩니다.)

➡️ successHandler 호출:

모든 인증 과정이 성공적으로 완료되었으므로, 필터는 사용자님이 설정한 oAuth2LoginSuccessHandler를 호출합니다.

이 핸들러가 JWT를 생성하고, 쿠키에 담고, 프론트엔드로 최종 리디렉션을 실행합니다.

3. 실패 시: failureHandler 호출
   만약 2번 OAuth2LoginAuthenticationFilter가 동작하는 과정 중

state 값이 일치하지 않거나,

Google이 에러 코드를 반환하거나,

customOAuth2UserService에서 예외(Exception)가 발생하면,

필터는 즉시 oAuth2LoginFailureHandler를 호출하여 로그인 실패 처리를 위임합니다.
