# DESIGN.md (최종)

## 1. 목적과 범위

- 시청은 WebRTC로 제공(저지연, p95 ≤ 2s).
- 녹화·쇼츠 소스는 HLS로 제공(안정·호환).
- 알림은 AI 워커가 카메라 RTSP를 직접 구독해 이벤트를 백엔드(Java)로 POST(시청/HLS와 독립).
- 구성은 모듈형. 예시: Nginx(공개) + MediaMTX(내부). 필요 시 AI 워커, 클리퍼, Redis 추가.

## 2. 전체 흐름

- 시청(WebRTC): IPCAM(RTSP) → MediaMTX(WebRTC) → Nginx(HTTPS) → 브라우저.
- 녹화/쇼츠(HLS): IPCAM(RTSP) → MediaMTX(HLS) → Nginx(HTTPS) → 저장/재생.
- 알림(AI): IPCAM(RTSP, 저지연 경로) → AI 워커 → 백엔드(Java) → 모바일 푸시(FCM/APNs). SMS는 보조.

## 3. 포트·노출 정책

- 외부 공개는 Nginx 80/443만.
- MediaMTX 포트 publish 금지(내부 네트워크/루프백만).
- RTSP는 내부망 한정 수신.
- Control API는 루프백 바인딩만 허용(외부 접근 불가).

## 4. 내부/외부 접속 모드

- 기본(INTERNAL): 사내/동일 서브넷 시청. STUN/TURN 미사용, WebRTC는 내부 라우팅으로만 동작.
- 외부(EXTERNAL, 필요 시에만):
  - Nginx는 공인 도메인/HTTPS로 WHEP/HLS만 프록시.
  - MediaMTX에 `webrtcAdditionalHosts: ['${PUBLIC_HOSTNAME}']` 설정.
  - NAT 환경에서 WebRTC가 필요하면 STUN/TURN 추가(그때 `webrtcICEServers2` 설정). 기본은 비활성.
  - 외부 모드 전환 시 지연 100–600ms 증가 가능.

## 5. MediaMTX 설정 원칙(구버전 키 제거)

- HLS(전역):
  - `hls: yes`, `hlsAddress: :${HLS_PORT}`, `hlsVariant: mpegts`
  - `hlsSegmentDuration: 4s`, `hlsSegmentCount: 55`, `hlsPartDuration: 1s`
  - `hlsAllowOrigin: '${PUBLIC_HOSTNAME}'`(운영 시 도메인 제한)
- WebRTC:
  - `webrtc: yes`, `webrtcAddress: :${WEBRTC_PORT}`, `webrtcEncryption: no`
  - `webrtcAdditionalHosts: ['${PUBLIC_HOSTNAME}']`(외부 모드 시 필요)
  - `webrtcICEServers2: []`(기본 비활성; 외부 필요해지면 STUN/TURN 추가)
- Control API:
  - `api: yes`, `apiAddress: 127.0.0.1:9997`(내부 전용)
- pathDefaults:
  - `useAbsoluteTimestamp: true`(m3u8에 PROGRAM‑DATE‑TIME 보장)
  - `sourceOnDemand: yes`
  - `rtspTransport: tcp`(안정성 우선)
- paths(한 번만 선언):
  - `paths.${STREAM_NAME}.source = ${RTSP_URL}`
- 금지: 존재하지 않는/구버전 키(예: `segmentDeleteAfter`, `hls.enabled`, `hls.address` 등).

## 6. 멀티 경로 설계(선택)

- 목표 지연/안정성에 따라 경로를 분리할 수 있습니다.
- 권장 네이밍:
  - `${STREAM_NAME}-rtc`: 시청(WebRTC) 전용. 필요 시 오디오를 Opus로 재퍼블리시(선택).
  - `${STREAM_NAME}-hls`: 녹화/쇼츠(HLS) 전용, TCP 안정성.
  - AI 경로: 기본은 카메라 RTSP를 워커가 직접 구독(초저지연). 필요 시 `${STREAM_NAME}-ai`(UDP pull) 프록시 경로를 추가할 수 있음.
- 카메라가 메인/서브 스트림을 제공하면: 시청/녹화는 메인, AI는 서브(저해상) 사용 권장(부하·지연 최적화).

## 7. on‑demand 동작과 상태

- `sourceOnDemand: yes`에서 리더가 없으면 HLS 생성은 멈춤(IDLE). index.m3u8 미갱신은 정상.
- 상태 라벨: `OFF`(관리자 끔) / `IDLE`(재생자 0) / `ACTIVE`(재생 중) / `ERROR`(입력 불능).
- 항상 HLS 필요 시:
  - `hlsAlwaysRemux: yes`, 또는
  - 내부 프로브가 주기적으로 `index.m3u8` 조회(리더 역할).

## 8. 상태 로그 수집(10초 주기)

- Control API `/v3/paths/get/${STREAM_NAME}`로 `sourceReady`, `readers`, `codecs` 수집.
- HLS 인덱스가 있을 때만 `index.m3u8` 파싱:
  - `#EXTINF` 개수 → `hls_segments`
  - 마지막 `#EXT-X-PROGRAM-DATE-TIME` → `last_segment_time`
  - `now - last_segment_time` → `est_lag_sec`
- fps는 선택(초기는 공란/정적 추정 허용).

## 9. 시간·전송 정책(2초 목표 반영)

- NTP 동기화 필수(카메라·서버).
- 시청(WebRTC): 저지연 최우선. 카메라 GOP≈1s, IDR 주기 짧게, B‑frame 최소/없음 권장. 오디오는 Opus가 최적. 필요 시 `runOnReady`로 `-c:v copy -c:a libopus` 재퍼블리시(선택).
- 녹화/쇼츠(HLS): 안정성 우선(TCP).
- 알림(AI): 초저지연(UDP, 무버퍼)로 RTSP 직접 구독. 워커는 `-rtsp_transport udp` 등 최소 버퍼 플래그 사용.

## 10. Nginx 프록시 정책

- 프록시 대상: HLS와 WebRTC 신호(WHEP)만. Control API는 프록시 금지.
- TLS 종료는 Nginx에서 처리.
- HLS MIME/CORS:
  - `m3u8: application/vnd.apple.mpegurl`
  - `ts: video/mp2t`
- WebRTC(WHEP) 프록시:
  - `proxy_request_buffering off`, `proxy_buffering off`, `proxy_http_version 1.1`

## 11. 환경 변수(최소)

- `STREAM_NAME`, `RTSP_URL`, `HLS_PORT`, `WEBRTC_PORT`, `PUBLIC_HOSTNAME`
- `hlsAllowOrigin`은 운영 도메인으로 제한 권장.

## 12. 수용 기준

- WebRTC 시청(1명 기준): p95 지연 ≤ 2초, 5분 안정 재생.
- HLS: 시작 2–6초, steady 8–12초, 5분 안정, PROGRAM‑DATE‑TIME 존재, 세그 50–60 유지.
- 알림: E2E p95 ≤ 2초(추론+POST+푸시, 내부망/엣지 기준).
- 로그: 10초 주기, OFF/IDLE/ACTIVE/ERROR 라벨과 핵심 지표가 시간에 따라 갱신.
- Control API 외부 접근 불가(루프백 바인딩).

## 13. 비범위(이번 턴 제외)

- AI 워커 구현, 이벤트 POST 스키마, 쇼츠 생성/요약, 푸시/SMS 연동.

## 14. Cursor 작업지시(코드 생성 범위)

- `mediamtx.template.yml`:
  - 5·7항 원칙 준수(전역 HLS/WebRTC/API 키, pathDefaults, paths 1회 선언).
  - `hlsVariant: mpegts`, `useAbsoluteTimestamp: true`, `webrtcICEServers2: []`.
- `nginx.conf`:
  - HLS와 WebRTC(WHEP)만 프록시. 80/443 노출.
  - HLS MIME 헤더 포함. WHEP 경로는 버퍼링 비활성/HTTP 1.1. Control API 미프록시.
- `entrypoint.sh`:
  - ENV 검증 → 템플릿 치환(`/mediamtx.yml`) → MediaMTX 실행
  - 10초 주기 상태 로그(API+m3u8 혼합) → 정상 종료 처리(trap)
- `.env.example`:
  - `STREAM_NAME`, `RTSP_URL`, `HLS_PORT`, `WEBRTC_PORT`, `PUBLIC_HOSTNAME`
  - 실제 `.env` 커밋 금지(운영은 Jenkins/서버 ENV 주입)
- `README.md`:
  - 기동/테스트/수용 기준
  - 구버전 키 미사용 명시, PROGRAM‑DATE‑TIME 확인 방법
  - 2초 지연 달성 조건(내부망, WebRTC, 카메라 GOP·IDR) 안내

## 15. 서버간(Java) 연동 계획(메모)

- 이벤트 수집(필수): AI 워커 → Java 백엔드
  - `POST /api/v1/events {cameraId, type, phase, startAt, endAt, confidence, attrs}`
- 상태 제공(선택): 게이트웨이 상태를 Java가 폴링 또는 게이트웨이가 Java로 푸시
  - 폴링: Java → 게이트웨이 사이드카(내부 전용) → MediaMTX Control API
  - 푸시: 게이트웨이 → Java `POST /api/v1/state`(OFF/IDLE/ACTIVE/ERROR, 지표 포함)
- 쇼츠 파이프라인(후행): Java가 트리거 → 클리퍼(FFmpeg/HLS 링버퍼) → 객체 저장(S3) → 메타 기록
- 접근 통제(후행): 서명 URL/토큰 기반(HLS/WebRTC 세션), Java가 발급

---

문제 가능성과 대응 요약
- 외부 접속: STUN/TURN 비활성 상태에선 NAT 넘는 WebRTC가 실패할 수 있음(내부망 우선). 외부 필요 시에만 STUN/TURN 추가.
- 코덱 호환: G.711/H.265 입력은 브라우저 호환 이슈. WebRTC는 Opus 오디오 재퍼블리시, HLS는 AAC/H.264 소스 권장(또는 변환 경로 준비).
- 카메라 부하: AI/RTC/HLS가 모두 카메라를 풀 시 부하 증가. 메인/서브스트림 분리 또는 MediaMTX 경로 재사용/프록시 전략 검토.
- on‑demand 헬스: 리더 없음(IDLE)일 때 m3u8 미갱신은 정상. 항상 헬스가 필요하면 `hlsAlwaysRemux` 또는 내부 프로브 사용.
- 시간 동기화: PROGRAM‑DATE‑TIME/클리핑 정확도는 NTP 동기화 의존.
