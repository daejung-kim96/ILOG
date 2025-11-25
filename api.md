# API 명세서

##  인증 서비스 (Auth Service)

### 1. 로그인

- **Endpoint**: `/api/v1/auth/login`
- **Method**: `POST`
- **Description**: 사용자 로그인 인증을 처리하고, Access Token과 Refresh Token을 쿠키에 담아 반환합니다.
- **Request**:
    - **Body**:
        ```json
        {
            "userId": "string",
            "password": "string"
        }
        ```
- **Response**:
    - **Success**:
        ```json
        {
            "status": 200,
            "Headers": [
                { "Set-Cookie": "access_token={ACCESS_TOKEN}; Path=/; HttpOnly; Secure; Max-Age=900; SameSite=Lax" },
                { "Set-Cookie": "refresh_token={REFRESH_TOKEN}; Path=/api/v1/auth/reissue; HttpOnly; Secure; Max-Age=604800; SameSite=Lax" }
            ],
            "body": {
                "code": 2000,
                "message": "요청에 성공했습니다.",
                "data": null
            }
        }
        ```
    - **Error**:
        ```json
        {
            "status": 401,
            "body": {
                "code": 4003,
                "message": "비밀번호가 일치하지 않습니다.",
                "data": null
            }
        }
        ```

### 2. 토큰 재발급

- **Endpoint**: `/api/v1/auth/reissue`
- **Method**: `POST`
- **Description**: Refresh Token을 이용하여 새로운 Access Token과 Refresh Token을 발급합니다.
- **Request**:
    - **Cookies**:
        - `refresh_token`: `string`
- **Response**:
    - **Success**:
        ```json
        {
            "status": 200,
            "Headers": [
                { "Set-Cookie": "access_token={NEW_ACCESS_TOKEN}; Path=/; HttpOnly; Secure; Max-Age=900; SameSite=Lax" },
                { "Set-Cookie": "refresh_token={NEW_REFRESH_TOKEN}; Path=/api/v1/auth/reissue; HttpOnly; Secure; Max-Age=604800; SameSite=Lax" }
            ],
            "body": {
                "code": 2000,
                "message": "요청에 성공했습니다.",
                "data": null
            }
        }
        ```
    - **Error**:
        ```json
        {
            "status": 401,
            "body": {
                "code": 4011,
                "message": "유효하지 않은 토큰입니다.",
                "data": null
            }
        }
        ```

---

## 사용자 서비스 (User Service)

### 1. 내 정보 조회

- **Endpoint**: `/api/v1/users/me`
- **Method**: `GET`
- **Description**: 현재 로그인한 사용자의 정보를 조회합니다.
- **Request**:
    - **Headers**:
        - `X-User-Id`: `string` (사용자 ID)
- **Response**:
    - **Success**:
        ```json
        {
            "status": 200,
            "body": {
                "code": 2000,
                "message": "요청에 성공했습니다.",
                "data": {
                    "userId": "string",
                    "name": "string",
                    "email": "string"
                }
            }
        }
        ```
    - **Error**:
        ```json
        {
            "status": 404,
            "body": {
                "code": 4002,
                "message": "존재하지 않는 사용자입니다.",
                "data": null
            }
        }
        ```