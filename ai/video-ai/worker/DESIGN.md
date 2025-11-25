# AI 워커 설계 문서

## 1. 시스템 구성

### 1.1 전체 아키텍처

```
[Camera RTSP] 
    ↓
[AI-Worker 컨테이너]
    ├─ 신호 추출: YOLO(person), YuNet(face), 모션, 오디오
    ├─ 상태머신: 이벤트 판정
    ├─ GET /config, PUT /config
    ├─ GET /preview
    └─ GET /events/stream (SSE)
    ↓
[테스트 페이지(정적 HTML)]
├─ 프리뷰 + ROI 편집 → PUT /config
├─ 임계값 슬라이더 → PUT /config
└─ 이벤트 로그 뷰어 ← /events/stream
```

### 1.2 데이터 흐름

```
RTSP 스트림
    ↓
프레임 추출 (OpenCV)
    ↓
┌─────────────────────────────────┐
│  병렬 신호 추출                 │
│  ├─ YOLO → person 신뢰도       │
│  ├─ YuNet → face 신뢰도        │
│  ├─ 배경차감 → 모션 에너지      │
│  └─ YAMNet → 오디오 분류        │
└─────────────────────────────────┘
    ↓
상태 머신 (이벤트 판정)
    ├─ 신호 값 확인
    ├─ 지속시간 확인
    └─ 이벤트 발생 판정
    ↓
이벤트 핸들러
    ├─ 로그 기록
    ├─ 스냅샷 저장
    └─ SSE 스트림 전송
    ↓
테스트 페이지 (실시간 확인)
```

---

## 2. 역할 분담

### 2.1 AI-Worker (컨테이너)

**주요 기능**:
1. RTSP 구독 및 프레임 추출
2. 신호 추출 (사람/얼굴/모션/오디오)
3. 상태 머신으로 이벤트 판정
4. 설정 관리 (ROI, 임계치) - 파일 저장
5. API 제공:
   - `GET /config`: 설정 읽기
   - `PUT /config`: 설정 저장
   - `GET /preview`: 최근 프레임 스냅샷
   - `GET /events/stream`: 이벤트 SSE 스트림
   - `GET /events?since=timestamp`: 이벤트 폴링

**기술 스택**:
- Python 3.11
- OpenCV (프레임 처리, YuNet)
- YOLO (ultralytics 또는 torchvision)
- TensorFlow/YAMNet (오디오 분류)
- FastAPI 또는 Flask (API 서버)

### 2.2 테스트 페이지 (정적 HTML)

**주요 기능**:
1. 프리뷰 탭: 최근 프레임 표시 + ROI 편집 (다각형)
2. 임계값 탭: 슬라이더로 임계값 조절
3. 이벤트 로그 탭: SSE로 실시간 이벤트 표시

**기술 스택**:
- 순수 HTML/JavaScript
- Canvas API (ROI 그리기)
- EventSource API (SSE 구독)
- Fetch API (설정 저장)

---

## 3. 이벤트 목록

### 3.1 1단계 구현 (확실히 가능한 이벤트)

#### 비전 이벤트
| ID | 한글 | 설명 | 신호 소스 |
|---|---|---|---|
| `NO_PERSON` | 무인 상태 | 사람이 감지되지 않음 | YOLO person 신뢰도 < 임계값 |
| `CAMERA_BLOCKED` | 카메라 가림/각도변화 | 전체 프레임 변화 감지 | 프레임 변화량 > 임계값 |
| `WAKE_UP` | 깨어남 | 모션 에너지 급증 | 모션 에너지 > 임계값 + 지속시간 |
| `LOW_ACTIVITY` | 무활동 | 모션 에너지 낮음 | 모션 에너지 < 임계값 + 지속시간 |
| `FACE_COVERED` | 얼굴 가림 지속 | 얼굴 신뢰도 낮음 | YuNet face 신뢰도 < 임계값 + 지속시간 |

#### 오디오 이벤트
| ID | 한글 | 설명 | 신호 소스 |
|---|---|---|---|
| `CRY` | 울음 | 울음 소리 감지 | YAMNet cry 확률 > 임계값 + 지속시간 |
| `IMPACT` | 큰 소리/충격음 | 에너지 피크 감지 | 오디오 에너지 피크 + 저주파 비중 |

### 3.2 2단계 구현 (추가 모델 필요)

- `PRONE_IDLE`: 엎드림+정지 (자세 인식 모델 필요)
- `GRIMACE`: 찡그림/불편 표정 (표정 인식 모델 필요)
- `STAND_UP`: 일어남 전이 (자세 변화 감지 필요)

**1단계에서는 제외하고 이후 확장**

---

## 4. 신호 추출 및 트리거 기준

### 4.1 신호 추출 방법

#### 사람 감지 (YOLO)
- 모델: YOLOv5 또는 YOLOv8 (경량 버전)
- 출력: person 클래스 신뢰도 (0.0 ~ 1.0)
- 프레임 스킵: 1초당 2~5프레임 분석

#### 얼굴 감지 (YuNet)
- 모델: OpenCV DNN 모델 (내장)
- 출력: 얼굴 신뢰도 (0.0 ~ 1.0)
- 프레임 스킵: 1초당 2~5프레임 분석

#### 모션 에너지 (배경차감)
- 방법: `cv2.createBackgroundSubtractorMOG2()`
- ROI 내 픽셀 변화량 계산
- 출력: 평균 모션 에너지 (0.0 ~ 1.0)
- 실시간 처리 가능

#### 오디오 분류 (YAMNet)
- 모델: TensorFlow Hub YAMNet
- 입력: 오디오 샘플 (0.96초)
- 출력: 521개 클래스 확률
- cry 클래스 확인 필요 (없으면 sound 이벤트로 처리)

#### 오디오 에너지 (IMPACT)
- 방법: 오디오 파형 분석
- 에너지 피크 감지
- 저주파 비중 계산
- 실시간 처리 가능

### 4.2 트리거 기준

**모든 이벤트는 "값 + 지속시간" 조합으로 판정**

예시:
- `NO_PERSON`: person 신뢰도 < 0.2 이고 5초 이상 지속
- `FACE_COVERED`: face 신뢰도 < 0.2 이고 3초 이상 지속
- `WAKE_UP`: 모션 에너지 > 임계값 이고 1.5초 이상 지속
- `CRY`: cry 확률 > 0.8 이고 1.5초 이상 지속

---

## 5. AI-Worker 외부 인터페이스

### 5.1 설정 엔드포인트

#### GET /config
- **설명**: 현재 설정 읽기
- **응답**: JSON 설정 객체
- **예시**:
```json
{
  "rtsp_url": "rtsp://...",
  "roi_polygon": [[100, 200], [300, 200], [300, 400], [100, 400]],
  "thresholds": { ... },
  "durations_ms": { ... },
  "policy": { ... }
}
```

#### PUT /config
- **설명**: 설정 저장 (전체 또는 일부 필드만)
- **요청 본문**: JSON 설정 객체 (부분 업데이트 가능)
- **응답**: 200 OK 또는 400 Bad Request

### 5.2 상태/미리보기

#### GET /preview
- **설명**: 최근 프레임(저해상도) 한 장
- **응답**: JPEG 이미지 (Content-Type: image/jpeg)
- **용도**: ROI 편집용 프리뷰

#### GET /events/stream
- **설명**: 이벤트 실시간 스트림 (SSE)
- **응답**: Server-Sent Events 스트림
- **포맷**: `data: {JSON}\n\n`
- **예시**:
```
data: {"id":"evt_001","type":"CRY","started_at":"2024-01-01T12:00:00Z",...}\n\n
```

#### GET /events?since=timestamp
- **설명**: 특정 시점 이후 이벤트 조회 (폴링용)
- **쿼리 파라미터**: `since` (ISO8601 타임스탬프)
- **응답**: JSON 배열
- **예시**:
```json
[
  {"id":"evt_001","type":"CRY",...},
  {"id":"evt_002","type":"WAKE_UP",...}
]
```

### 5.3 이벤트 포맷

```json
{
  "id": "evt_001",                    // 이벤트 ID
  "level": "info",                    // info | warning | critical
  "started_at": "2024-01-01T12:00:00.123Z",
  "ended_at": null,                   // 진행 중이면 null
  "type": "CRY",                      // 이벤트 타입
  "reason_codes": "cry_prob:0.95,duration:1.5s",  // 신호·지속시간 요약
  "snapshot_url": "/snapshots/evt_001.jpg"  // 선택 (스냅샷 경로)
}
```

---

## 6. 설정 스키마

### 6.1 전체 설정 구조

```json
{
  "rtsp_url": "rtsp://admin:password@10.91.8.117:554/stream_ch00_0",
  "roi_polygon": [
    [100, 200],
    [300, 200],
    [300, 400],
    [100, 400]
  ],
  "thresholds": {
    "person_conf_min": 0.2,
    "face_conf_min": 0.2,
    "wake_flow": 0.5,
    "low_flow": 0.1,
    "cry_on": 0.8,
    "cry_off": 0.3,
    "impact_energy": 0.7
  },
  "durations_ms": {
    "no_person": 5000,
    "camera_blocked": 3000,
    "face_covered": 3000,
    "wake_up": 1500,
    "low_activity": 5000,
    "cry": 1500,
    "impact": 200
  },
  "policy": {
    "cooldown_ms": 30000,
    "enabled": {
      "NO_PERSON": true,
      "CAMERA_BLOCKED": true,
      "WAKE_UP": true,
      "LOW_ACTIVITY": true,
      "FACE_COVERED": true,
      "CRY": true,
      "IMPACT": true
    }
  }
}
```

### 6.2 필드 설명

#### rtsp_url
- 카메라 RTSP 주소
- 형식: `rtsp://[user:password@]host:port/path`

#### roi_polygon
- 침대 영역 좌표 리스트
- 형식: `[[x1,y1], [x2,y2], ...]`
- 좌표계: 프레임 픽셀 좌표

#### thresholds
- 신호 임계값
- `person_conf_min`: 무인 판단 기준 (0.0 ~ 1.0)
- `face_conf_min`: 얼굴 가림 판단 기준 (0.0 ~ 1.0)
- `wake_flow`: 깨어남 모션 임계값 (0.0 ~ 1.0)
- `low_flow`: 무활동 모션 임계값 (0.0 ~ 1.0)
- `cry_on`: 울음 시작 임계값 (0.0 ~ 1.0)
- `cry_off`: 울음 해제 임계값 (0.0 ~ 1.0)
- `impact_energy`: 충격음 에너지 임계값 (0.0 ~ 1.0)

#### durations_ms
- 이벤트별 최소 지속시간 (밀리초)
- 각 이벤트 타입별로 설정

#### policy
- `cooldown_ms`: 이벤트 쿨다운 시간 (밀리초)
- `enabled`: 이벤트별 활성화 플래그

### 6.3 설정 저장 방식

**파일 저장** (`config.json`):
- AI 워커가 시작 시 파일 읽기
- `PUT /config` 호출 시 파일 업데이트
- 파일 경로: `/app/config/config.json`

---

## 7. 테스트 페이지 요구사항

### 7.1 화면 구성

#### 탭 1: 프리뷰
- **최근 프레임 표시**: `GET /preview` 이미지 표시
- **ROI 편집 도구**:
  - 다각형 그리기 (마우스 클릭으로 점 추가)
  - 점 이동 (드래그)
  - 점 삭제 (우클릭 또는 Delete 키)
  - 다각형 삭제 (전체 삭제 버튼)
- **저장 버튼**: `PUT /config`로 `roi_polygon` 업데이트

#### 탭 2: 임계값
- **슬라이더 목록**:
  - person_conf_min (0.0 ~ 1.0)
  - face_conf_min (0.0 ~ 1.0)
  - wake_flow (0.0 ~ 1.0)
  - low_flow (0.0 ~ 1.0)
  - cry_on (0.0 ~ 1.0)
  - cry_off (0.0 ~ 1.0)
  - impact_energy (0.0 ~ 1.0)
  - 각 이벤트별 지속시간 (밀리초)
  - cooldown_ms (밀리초)
- **저장 버튼**: `PUT /config`로 `thresholds`, `durations_ms`, `policy` 업데이트

#### 탭 3: 이벤트 로그
- **이벤트 테이블**:
  - 컬럼: 시간, 타입, 레벨, 상태, reason_codes
  - SSE 스트림으로 실시간 추가
  - 스크롤 가능
- **이벤트 상세**:
  - 클릭 시 reason_codes와 스냅샷 표시
  - 스냅샷 이미지 표시 (있으면)

### 7.2 동작 요구사항

#### 초기 로드
1. `GET /config` 호출
2. 현재 설정값 반영:
   - 프리뷰 탭: ROI 다각형 그리기
   - 임계값 탭: 슬라이더 값 설정
3. `GET /events/stream` 연결 (SSE)

#### 저장 동작
1. 사용자가 저장 버튼 클릭
2. `PUT /config` 호출 (변경된 필드만 전송)
3. 성공: 토스트 메시지 표시
4. 실패: 에러 메시지 표시

#### 이벤트 스트림
1. EventSource로 `/events/stream` 구독
2. 이벤트 수신 시 테이블에 추가
3. 연결 끊김 시 자동 재연결

---

## 8. 초기 캘리브레이션 절차

### 8.1 ROI 지정

1. **프리뷰 탭 열기**
2. **침대 영역만 둘러 ROI 다각형 그리기**
   - 아이 없는 장면에서 그리기
   - 그늘, 담요 포함해도 됨
   - 배경이 섞이지 않도록 침대 경계만 포함
3. **저장 버튼 클릭**

### 8.2 기준선 수집 (각 10~20분)

**수집 시나리오**:
- 낮/밤
- 조명 켜고/끄고
- 담요 덮음/벗김
- 아이 있음/없음

**이때**:
- 이벤트는 로그만 남기고 알림은 꺼도 됨
- 모션 에너지 평균값 기록
- 오디오 레벨 평균값 기록

### 8.3 임계 초안 설정

#### 비전 임계값
- `person_conf_min`: 0.2
- `face_conf_min`: 0.2
- `wake_flow`: 해당 환경 평균 모션의 약 2배
- `low_flow`: 평균의 약 0.4배

#### 지속시간
- `no_person`: 5000ms
- `face_covered`: 3000ms
- `wake_up`: 1500~2000ms
- `low_activity`: 5000ms

#### 오디오 임계값
- `cry_on`: 1.5초 평균 0.8
- `cry_off`: 2.0초 평균 0.3
- `impact_energy`: 0.2초 내 피크

### 8.4 검증

1. **녹화 리플레이**
   - 녹화 영상 재생하면서 이벤트 로그 확인
   - 체감과 맞는지 확인

2. **임계값 조정**
   - **FP(False Positive)가 잦으면**: 임계값↑ 또는 지속시간↑
   - **FN(False Negative)이면**: 임계값↓ 또는 지속시간↓

### 8.5 운영 설정

#### 크리티컬 이벤트 쿨다운
- `PRONE_IDLE`, `NO_PERSON`, `CAMERA_BLOCKED`, `CRY`, `IMPACT`
- 쿨다운: 30~60초

#### 중복 억제
- `WAKE_UP`과 `STAND_UP` 동시 발생 시 하나로 합치기
- 같은 이벤트가 쿨다운 내 반복 발생 시 무시

---

## 9. 기술 스택 선택

### 9.1 AI 모델

#### YOLO (Person Detection)
- **선택**: YOLOv8n (nano 버전) 또는 YOLOv5s
- **라이브러리**: `ultralytics` (YOLOv8) 또는 `torchvision` (YOLOv5)
- **이유**: 경량, 실시간 처리 가능
- **대안**: 없음 (표준)

#### YuNet (Face Detection)
- **선택**: OpenCV DNN 모델 (내장)
- **라이브러리**: `cv2.dnn.readNet()`
- **이유**: 경량, OpenCV에 포함
- **대안**: 없음 (표준)

#### 모션 에너지
- **선택**: 배경차감 (MOG2)
- **라이브러리**: `cv2.createBackgroundSubtractorMOG2()`
- **이유**: 실시간 처리 가능, 계산 비용 낮음
- **대안**: 광류(Optical Flow) - 계산 비용 높음

#### 오디오 분류
- **선택**: YAMNet
- **라이브러리**: TensorFlow Hub
- **이유**: 경량, 실시간 처리 가능
- **대안**: 없음 (표준)

### 9.2 API 서버

#### 선택: FastAPI
- **이유**: 
  - 비동기 지원 (SSE 스트림에 유리)
  - 자동 API 문서 생성
  - 성능 우수
- **대안**: Flask (단순하지만 비동기 지원 약함)

### 9.3 프레임 처리

#### 선택: OpenCV
- **이유**: RTSP 구독, 프레임 처리, 배경차감 모두 지원
- **대안**: 없음 (표준)

---

## 10. 프로젝트 구조

```
AI/ai/video-ai/worker/
├── Dockerfile
├── requirements.txt
├── .env.example
├── main.py                    # FastAPI 앱 진입점
├── src/
│   ├── __init__.py
│   ├── rtsp_client.py        # RTSP 구독 모듈
│   ├── frame_processor.py     # 프레임 전처리
│   ├── signal_extractor.py    # 신호 추출 (YOLO, YuNet, 모션, 오디오)
│   ├── event_detector.py      # 이벤트 감지 로직 (상태 머신)
│   ├── event_handler.py       # 이벤트 처리 (로그, 스냅샷)
│   ├── config_manager.py      # 설정 관리 (파일 읽기/쓰기)
│   └── api/
│       ├── __init__.py
│       ├── config.py          # GET/PUT /config
│       ├── preview.py          # GET /preview
│       └── events.py           # GET /events/stream, GET /events
├── models/                    # AI 모델 파일
│   ├── yolo/                  # YOLO 모델
│   └── yamnet/                # YAMNet 모델
├── config/
│   └── config.json            # 설정 파일 (기본값)
├── logs/                      # 로그 파일
│   └── events.log
├── snapshots/                 # 이벤트 스냅샷
└── test-page/                 # 테스트 페이지
    └── index.html
```

---

## 11. 환경변수 설정

```bash
# RTSP 설정
RTSP_URL=rtsp://admin:password@10.91.8.117:554/stream_ch00_0
CAMERA_ID=c200

# AI 모델 설정
YOLO_MODEL_PATH=/app/models/yolo/yolov8n.pt
YUNET_MODEL_PATH=/app/models/yunet/face_detection_yunet.onnx
YAMNET_MODEL_PATH=/app/models/yamnet/

# 설정 파일
CONFIG_FILE=/app/config/config.json

# 성능 설정
FRAME_SKIP=5  # N 프레임마다 분석
MAX_FRAME_BUFFER=10
PREVIEW_WIDTH=640  # 프리뷰 이미지 너비
PREVIEW_HEIGHT=480  # 프리뷰 이미지 높이

# 로그 설정
LOG_EVENTS=true
EVENTS_LOG_FILE=/app/logs/events.log
SNAPSHOTS_DIR=/app/snapshots

# API 설정
API_HOST=0.0.0.0
API_PORT=8000
```

---

## 12. 구현 순서

### 12.1 기본 구조 (1일차)
1. 프로젝트 디렉토리 생성
2. Dockerfile, requirements.txt 작성
3. FastAPI 기본 구조
4. 환경변수 설정

### 12.2 RTSP 클라이언트 (1일차)
1. OpenCV로 RTSP 구독
2. 프레임 추출
3. 재연결 로직

### 12.3 신호 추출 (2일차)
1. YOLO person detection
2. YuNet face detection
3. 배경차감 모션 에너지
4. YAMNet 오디오 분류

### 12.4 이벤트 감지기 (2일차)
1. 상태 머신 구현
2. 임계값 기반 감지
3. 지속시간 확인
4. 이벤트 정보 수집

### 12.5 이벤트 핸들러 (3일차)
1. 이벤트 로그 기록
2. 스냅샷 저장
3. SSE 스트림 전송

### 12.6 설정 관리 (3일차)
1. 설정 파일 읽기/쓰기
2. GET /config API
3. PUT /config API

### 12.7 API 엔드포인트 (3일차)
1. GET /preview
2. GET /events/stream (SSE)
3. GET /events (폴링)

### 12.8 테스트 페이지 (4일차)
1. 프리뷰 탭 (ROI 편집)
2. 임계값 탭 (슬라이더)
3. 이벤트 로그 탭 (SSE 구독)

### 12.9 통합 및 테스트 (4일차)
1. 전체 파이프라인 통합
2. 이벤트 감지 확인
3. 성능 테스트
4. 지연 측정

---

## 13. 현실성 검토

### 13.1 가능한 기술

#### ✅ 확실히 가능
- **YOLO person detection**: 경량 모델, 실시간 처리 가능
- **YuNet face detection**: OpenCV 내장, 실시간 처리 가능
- **배경차감 모션**: OpenCV 제공, 실시간 처리 가능
- **YAMNet 오디오**: 경량 모델, 실시간 처리 가능
- **상태 머신**: 로직 구현, 문제 없음
- **API 서버**: FastAPI, 문제 없음
- **SSE 스트림**: FastAPI 지원, 문제 없음

#### ⚠️ 추가 작업 필요
- **PRONE_IDLE**: 자세 인식 모델 추가 필요 (1단계 제외)
- **GRIMACE**: 표정 인식 모델 추가 필요 (1단계 제외)
- **STAND_UP**: 자세 변화 감지 로직 추가 필요 (1단계 제외)

### 13.2 성능 예상

#### CPU만 사용 시
- 프레임 처리: 1초당 2~3프레임 분석 가능
- 지연: 약 1~2초
- 메모리: 약 500MB~1GB

#### GPU 사용 시
- 프레임 처리: 1초당 10프레임 이상 가능
- 지연: 약 0.5초 이하
- 메모리: 약 1~2GB

### 13.3 이벤트 인식 확인 방법

#### 로컬 테스트
1. **이벤트 로그 파일**: `/app/logs/events.log` (JSON 형식)
2. **콘솔 출력**: 이벤트 발생 시 즉시 출력
3. **스냅샷 저장**: `/app/snapshots/` 디렉토리
4. **테스트 페이지**: SSE 스트림으로 실시간 확인

#### 검증 방법
- 녹화 리플레이: 녹화 영상 재생하면서 이벤트 로그 확인
- 수동 트리거: 테스트 시나리오 실행 후 이벤트 확인

---

## 14. 주의사항

### 14.1 카메라 부하
- 게이트웨이(HLS/WebRTC)와 워커가 동시에 RTSP 구독
- 가능하면 서브 스트림 사용 (저해상도)

### 14.2 네트워크
- RTSP는 내부망 전용
- UDP 전송 사용 (지연 최소화)

### 14.3 시간 동기화
- NTP 동기화 필수
- 타임스탬프 정확도 중요

### 14.4 모델 선택
- 실시간 추론 가능한 경량 모델
- 정확도와 성능의 균형

### 14.5 설정 저장
- 파일 저장 방식 사용 (`config.json`)
- 컨테이너 재시작 시에도 설정 유지

---

## 15. 테스트 계획

### 15.1 단위 테스트
- RTSP 클라이언트 연결 테스트
- YOLO/YuNet 모델 로딩 테스트
- 신호 추출 로직 테스트
- 이벤트 감지 로직 테스트
- 설정 관리 테스트
- API 엔드포인트 테스트

### 15.2 통합 테스트
- 전체 파이프라인 테스트
- 이벤트 감지 확인
- 알람 로그 확인
- 스냅샷 저장 확인
- 지연 측정 (감지 지연 p95 ≤ 2초)
- 장시간 안정성 테스트 (24시간)

### 15.3 성능 테스트
- 프레임 처리 속도
- 메모리 사용량
- CPU 사용량
- 이벤트 판정 정확도

---

## 16. 향후 확장 계획

### 16.1 다음 단계 (Java 백엔드 연동)
- 백엔드 API 연동
- 실제 알람 발송 시스템
- 쇼츠 생성 파이프라인 연동
- 이벤트 히스토리 저장

### 16.2 추가 이벤트 (2단계)
- `PRONE_IDLE`: 자세 인식 모델 추가
- `GRIMACE`: 표정 인식 모델 추가
- `STAND_UP`: 자세 변화 감지 로직 추가

### 16.3 장기 확장
- 다중 카메라 지원
- 이벤트 타입 확장
- 모델 학습/업데이트 파이프라인
- 실시간 대시보드

---

## 17. 참고 자료

- YOLO: https://github.com/ultralytics/ultralytics
- YuNet: https://github.com/opencv/opencv_zoo/tree/master/models/face_detection_yunet
- YAMNet: https://www.tensorflow.org/hub/tutorials/yamnet
- FastAPI: https://fastapi.tiangolo.com/
- OpenCV: https://docs.opencv.org/
- Server-Sent Events: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events

---

## 18. 구현 체크리스트

### 필수 구현
- [ ] RTSP 클라이언트 (OpenCV)
- [ ] YOLO person detection
- [ ] YuNet face detection
- [ ] 배경차감 모션 에너지
- [ ] YAMNet 오디오 분류
- [ ] 상태 머신 (이벤트 판정)
- [ ] 이벤트 핸들러 (로그, 스냅샷)
- [ ] 설정 관리 (파일 저장)
- [ ] FastAPI 서버
- [ ] GET /config, PUT /config
- [ ] GET /preview
- [ ] GET /events/stream (SSE)
- [ ] GET /events (폴링)
- [ ] 테스트 페이지 (프리뷰, 임계값, 이벤트 로그)

### 선택 구현 (이후)
- [ ] Java 백엔드 API 연동
- [ ] PRONE_IDLE 이벤트
- [ ] GRIMACE 이벤트
- [ ] STAND_UP 이벤트



