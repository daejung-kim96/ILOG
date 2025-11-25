| domain              | 개발 현황       | 기능                 | method | API path                                               | 담당자     | auth |
|---------------------|----------------|----------------------|--------|--------------------------------------------------------|-----------|------|
| auth                | ✅ Done        | 로그인               | POST   | /oauth2/authorization/google                           | 이승주    |      |
| auth                | ✅ Done        | 리프레시 토큰 재발급 | POST   | /api/v1/auth/reissue                                   | 이승주    | ✅   |
| user                | ✅ Done        | 내 정보 조회         | GET    | /api/v1/users/me                                       | 이승주    | ✅   |
| user                | 🔄 In progress | 회원가입             | POST   | /api/v1/auth/signup                                    | 장동현     |      |
| WatchBaby-상태확인   | ⏸ Not started  | 카메라 목록          | GET    | /api/v1/cameras                                        | 김종재 |      |
| WatchBaby-상태확인   | ⏸ Not started  | 카메라 등록          | POST   | /api/v1/cameras                                        | 김종재 |      |
| WatchBaby-상태확인   | ⏸ Not started  | ROI 설정             | PUT    | /api/v1/cameras/{id}/roi                               | 김종재 |      |
| WatchBaby-상태확인   | ⏸ Not started  | 실시간 상태 스트림   | GET    | /api/v1/state/latest?cameraId=...                      | 김종재 |      |
| WatchBaby-상태확인   | ⏸ Not started  | 최신 상태 조회       | GET    | /api/v1/state/latest?cameraId=...                      | 김종재 |      |
| WatchBaby-쇼츠       | ⏸ Not started  | 이벤트 수신          | POST   | /api/v1/events                                         | 김종재 |      |
| WatchBaby-쇼츠       | ⏸ Not started  | 이벤트 조회          | GET    | /api/v1/events?cameraId=...&from=...&to=...&type=...   | 김종재 |      |
| WatchBaby-쇼츠     | ⏸ Not started  | 클립 생성       | POST   | /api/v1/clips                                     | 김종재  |      |
| WatchBaby-쇼츠     | ⏸ Not started  | 클립 조회       | GET    | /api/v1/clips/{id}                                | 김종재  |      |
| WatchBaby-쇼츠     | ⏸ Not started  | 다운로드 URL    | GET    | /api/v1/clips/{id}/signed-url                     | 김종재  |      |
| WatchBaby-알림     | ⏸ Not started  | 웹푸시 구독     | POST   | /api/v1/push/subscribe                            | 김종재  |      |
| WatchBaby-알림     | ⏸ Not started  | 알림 설정       | PUT    | /api/v1/settings/alert                            | 김종재  |      |
| WatchBaby-알림     | ⏸ Not started  | 알림 히스토리   | GET    | /api/v1/alerts?from=...&to=...                    | 김종재  |      |
| WatchBaby-알림     | ⏸ Not started  | 알림 테스트     | POST   | /api/v1/alerts/test                                | 김종재  |      |
| diary  | ⏸ Not started  | 일기 리스트 조회 | GET    | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 일기 작성       | POST   | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 일기 상세 조회   | GET    | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 일기 수정       | PUT    | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 일기 삭제       | DELETE | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 댓글 리스트 조회 | GET    | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 댓글 작성       | POST   | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 댓글 수정       | PUT    | /api/v1/   | 장동현  | ✅   |
| diary  | ⏸ Not started  | 댓글 삭제       | DELETE | /api/v1/   | 장동현  | ✅   |
