# 아이로그 (ilog)

## 프로젝트 개요

### 🎯 프로젝트 목적
육아 관찰 및 기록과 산모 보호를 위한 **통합 육아 플랫폼**으로, 다음과 같은 기능을 제공합니다:
- **사용자 인증**: OAuth 2.0 기반 Google, Kakao, Naver 소셜 로그인
- **육아 일기**: 육아 관련 내용 및 이미지/비디오 첨부
- **감정 분석**: 일기 기반 산모 감정 분석
- **육아 앨범**: 일기에 첨부된 사진을 통한 육아 앨범 생성
- **실시간 스트리밍**: WebRTC 기반 아이 실시간 관찰
- **쇼츠**: 아이의 특수한 행동을 쇼츠로 기록

### 🔧 주요 기술 스택
- **백엔드**: Spring Boot 3.5.7 (Java 21)
- **프론트엔드**: React 19.0.0 + TypeScript 5.6.0 + Vite 7.0.0
- **데이터베이스**: PostgreSQL 14+
- **캐시**: Redis 7-alpine
- **메시지 큐**: Apache Kafka 7.5.0 (KRaft 모드)
- **AI/ML**: FastAPI + Transformers (감정 분석)
- **실시간 스트리밍**: MediaMTX (WebRTC/HLS)
- **객체 저장소**: MinIO (S3 호환)
- **컨테이너화**: Docker + Docker Compose
- **CI/CD**: Jenkins
- **웹서버**: Nginx 1.27
- **CDC(Change Data Capture)**: Debezium Kafka Connect

---

## 시스템 아키텍처

### 🏗️ 전체 아키텍처
```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                         │
│                    (Vite 3000 / Nginx 80/443)                   │
└────────────────┬────────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                 API GATEWAY (Spring Boot)                        │
│                    (Port 8080)                                  │
└────────┬────────────┬────────────┬─────────────┬────────────────┘
         │            │            │             │
         ▼            ▼            ▼             ▼
    ┌────────┐   ┌───────┐   ┌────────┐   ┌──────────┐
    │ Auth   │   │User   │   │Backend │   │Group     │
    │Service │   │Service│   │Service │   │Service   │
    │(8080)  │   │(8080) │   │(8080)  │   │(8080)    │
    └────────┘   └───────┘   └────────┘   └──────────┘
         │            │            │             │
         └────────────┴────────────┴─────────────┘
                      │
                      ▼
         ┌─────────────────────────────┐
         │     PostgreSQL (5432)       │
         │  - auth_write/auth_read     │
         │  - user_write/user_read     │
         │  - group_write/group_read   │
         │  - backend schemas          │
         └──────────┬──────────────────┘
                    │
         ┌──────────┴──────────┐
         ▼                     ▼
    ┌─────────┐            ┌──────────┐
    │ Kafka   │            │ MinIO    │
    │ (KRaft) │            │ (S3 API) │
    │ 3 nodes │            │(9000)    │
    └─────────┘            └──────────┘
         │
         ▼
    ┌─────────────────┐
    │ Debezium        │
    │ Kafka Connect   │
    │ (8083)          │
    └─────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    AI Services                                  │
├─────────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────┐  ┌──────────────────────────────┐ │
│ │   Emotion AI (FastAPI)   │  │   Video AI (MediaMTX)        │ │
│ │   - Sentiment Analysis   │  │   - WebRTC Streaming         │ │
│ │   - Transformers Model   │  │   - HLS Recording            │ │
│ │   (8000)                 │  │   (8555 WebRTC / 8888 HLS)   │ │
│ └──────────────────────────┘  └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 🔄 데이터 플로우
1. **사용자 인증**: Frontend → Gateway → Auth Service → PostgreSQL
2. **데이터 동기화**: Auth Write → Debezium → Kafka → Auth Read (CQRS 패턴)
3. **그룹 관리**: User Service → Kafka → Group Service (이벤트 기반)
4. **감정 분석**: Backend Service → Emotion AI → OpenAI API (선택)
5. **파일 저장**: Backend Service → MinIO (S3 호환 스토리지)
6. **실시간 스트리밍**: IP Camera → MediaMTX → Browser (WebRTC/HLS)

---

## 환경 요구사항

### 💻 하드웨어 요구사항
- **CPU**: 최소 4코어 (권장 8코어 이상)
- **메모리**: 최소 8GB RAM (권장 16GB 이상)
- **저장공간**: 최소 50GB 여유 공간
- **네트워크**: 안정적인 인터넷 연결 (실시간 스트리밍 시 최소 5Mbps)

### 🖥️ 소프트웨어 요구사항
- **운영체제**: Ubuntu 20.04+ / CentOS 8+ / Windows 10+ WSL2 / macOS 10.15+
- **Docker**: 20.10.0+
- **Docker Compose**: 2.0.0+
- **Git**: 2.25.0+
- **Java**: OpenJDK 21+ (로컬 개발 시)
- **Node.js**: 18.0.0+ (프론트엔드 개발 시)
- **Python**: 3.9+ (AI 서비스 개발 시)

### 🌐 외부 서비스 요구사항
- **Google OAuth**: OAuth 2.0 설정 (https://console.cloud.google.com/)
- **Kakao OAuth**: OAuth 2.0 설정 (https://developers.kakao.com/)
- **OpenAI API** (선택): GPT 모델 사용을 위한 API 키 (선택사항)

---

## 설치 및 배포 가이드

### 1️⃣ 저장소 클론
```bash
git clone https://lab.ssafy.com/s13-bigdata-dist-sub1/S13P31A108.git
cd S13P31A108
```

### 2️⃣ 환경 변수 설정
```bash
# 프로젝트 루트에서 .env 파일 생성
cat > .env << 'EOF'
# PostgreSQL 설정
POSTGRES_DB=stock_db
POSTGRES_USER=stock_user
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_EXTERNAL_PORT=5432

# Kafka 설정
KAFKA_CLUSTER_ID=MkQkVTcxOGQwMDZjMjU1NTBh
KAFKA_EXTERNAL_PORT_1=29092
KAFKA_EXTERNAL_PORT_2=29093
KAFKA_EXTERNAL_PORT_3=29094

# Kafka Connect 설정
CONNECT_EXTERNAL_PORT=8083

# Redis 설정
REDIS_PASSWORD=your_redis_password

# MinIO 설정
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password

# Spring Boot 설정
SPRING_PROFILE=prod
SERVER_PORT=8080

# JWT 설정
JWT_ECC_PRIVATE_KEY=your_private_key_here
JWT_ECC_PUBLIC_KEY=your_public_key_here

# OAuth 설정
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_google_client_secret

# 도메인 설정
DOMAIN_URL=http://localhost

# AI 서비스 설정
AI_EMOTION_BASE_URL=http://emotion-ai:8000

# LLM 설정 (선택사항)
LLM_API_BASE=https://api.openai.com/v1
LLM_API_KEY=your_openai_api_key
LLM_MODEL=gpt-4o
LLM_TIMEOUT_MS=30000
EOF
```

### 3️⃣ Docker 환경 확인
```bash
# Docker 설치 확인
docker --version
docker-compose --version

# Docker 서비스 시작 (Linux)
sudo systemctl start docker
sudo systemctl enable docker
```

### 4️⃣ 수동 배포 (단계별)
```bash
# 1. 기존 컨테이너 정리
docker-compose down --remove-orphans

# 2. 이미지 빌드
docker-compose build --no-cache

# 3. 개발 환경 시작
docker-compose up -d

# 또는 운영 환경 시작 (SSL, 성능 최적화)
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 4. 헬스체크
docker-compose ps
```

### 5️⃣ 배포 후 검증
```bash
# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f gateway
docker-compose logs -f auth
docker-compose logs -f backend

# 포트 확인
netstat -tulpn | grep -E '80|8080|5432|9000'
```

---

## 환경 설정

### 🔧 필수 환경 변수

#### PostgreSQL 설정
```bash
# 데이터베이스
POSTGRES_DB=stock_db
POSTGRES_USER=stock_user
POSTGRES_PASSWORD=your_secure_password
POSTGRES_EXTERNAL_PORT=5432
```

#### Kafka 설정
```bash
# Kafka 클러스터 (KRaft 모드)
KAFKA_CLUSTER_ID=MkQkVTcxOGQwMDZjMjU1NTBh
KAFKA_EXTERNAL_PORT_1=29092
KAFKA_EXTERNAL_PORT_2=29093
KAFKA_EXTERNAL_PORT_3=29094
```

#### Redis 설정
```bash
# Redis (인메모리 캐시, Auth 서비스 전용)
REDIS_PASSWORD=your_redis_password
```

#### MinIO 설정
```bash
# MinIO (S3 호환 객체 스토리지)
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=your_minio_password
```

#### Spring Boot 공통 설정
```bash
# 애플리케이션
SPRING_PROFILE=prod
SERVER_PORT=8080

# JWT (ECC 기반)
JWT_ECC_PRIVATE_KEY=-----BEGIN EC PRIVATE KEY-----\n...\n-----END EC PRIVATE KEY-----
JWT_ECC_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----
```

#### OAuth 설정
```bash
# Google OAuth
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_client_secret

# Kakao OAuth (향후 추가)
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
```

#### AI 서비스 설정
```bash
# Emotion AI (FastAPI)
AI_EMOTION_BASE_URL=http://emotion-ai:8000
MODEL_DIR=out_roberta_60cls/checkpoint-6843
MODEL_VERSION=roberta-kr-6cls-ctx:ckpt-6843
PIPELINE_ALPHA=0.7
MAX_LEN=256

# LLM (OpenAI 연동, 선택사항)
LLM_API_BASE=https://api.openai.com/v1
LLM_API_KEY=your_openai_api_key
LLM_MODEL=gpt-4o
LLM_TIMEOUT_MS=30000
```

#### 기타 설정
```bash
# 도메인
DOMAIN_URL=http://localhost

# 타임존
TZ=Asia/Seoul
```

### 🌐 네트워크 설정

#### 포트 매핑
| 서비스 | 포트 | 설명 |
|--------|------|------|
| Nginx | 80, 443 | 웹 서버 (HTTP/HTTPS) |
| Gateway | 8080 | API Gateway |
| Backend | 내부 | 백엔드 서비스 |
| Auth | 내부 | 인증 서비스 |
| User | 내부 | 사용자 서비스 |
| Group | 내부 | 그룹 서비스 |
| PostgreSQL | 5432 | 주 데이터베이스 |
| Kafka 1-3 | 29092-29094 | Kafka 브로커 |
| Kafka Connect | 8083 | Debezium CDC |
| Redis | 6379 | 캐시 (Auth 용) |
| MinIO | 9000, 9001 | 객체 스토리지 |
| Emotion AI | 8000 | 감정 분석 API |
| MediaMTX (WebRTC) | 8555 | 실시간 스트리밍 |
| MediaMTX (HLS) | 8888 | HLS 스트리밍 |

#### 방화벽 설정 (Linux)
```bash
# 필요한 포트 열기
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw allow 5432/tcp

# 특정 IP에서만 DB 접근 허용
sudo ufw allow from 10.0.0.0/8 to any port 5432
```

---

## 서비스별 구성

### 🔐 Auth Service (인증 서비스)

#### 주요 기능
- OAuth 2.0 기반 소셜 로그인 (Google, Kakao)
- JWT 토큰 발급 및 갱신 (ECC 기반)
- 사용자 프로필 관리
- Redis 기반 토큰 블랙리스트

#### 기술 스택
- Spring Boot 3.5.7
- Spring Security + OAuth2
- Spring Data JPA
- JWT (JSON Web Token)
- Redis
- PostgreSQL (CQRS 패턴: Write/Read 스키마 분리)

#### 빌드 및 실행
```bash
cd auth

# 개발 환경
./gradlew bootRun

# 운영 환경
./gradlew clean bootJar
java -jar build/libs/auth-*.jar
```

#### 설정 파일
- `auth/src/main/resources/application.yml` - 기본 설정
- `auth/src/main/resources/application-prod.yml` - 운영 설정

---

### 👤 User Service (사용자 서비스)

#### 주요 기능
- 사용자 정보 관리
- 사용자 프로필 조회/수정
- 사용자 통계 조회
- Kafka 기반 이벤트 소비

#### 기술 스택
- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL (CQRS 패턴: Write/Read 스키마 분리)
- Kafka Consumer

#### 빌드 및 실행
```bash
cd user

# 개발 환경
./gradlew bootRun

# 운영 환경
./gradlew clean bootJar
java -jar build/libs/user-*.jar
```

---

### 💼 Backend Service (백엔드 메인 서비스)

#### 주요 기능
- 주식 정보 조회
- 포트폴리오 관리
- 거래 기록 관리
- 뉴스 및 마켓 데이터 처리
- 감정 분석 API 연동
- MinIO 파일 관리

#### 기술 스택
- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL
- Kafka Producer/Consumer
- AWS SDK (S3 호환)
- RestTemplate / WebClient (AI 서비스 연동)

#### 빌드 및 실행
```bash
cd backend

# 개발 환경
./gradlew bootRun

# 운영 환경
./gradlew clean bootJar
java -jar build/libs/backend-*.jar
```

#### 설정 파일
- `backend/src/main/resources/application.yml` - 기본 설정
- `backend/src/main/resources/application-prod.yml` - 운영 설정

---

### 👥 Group Service (그룹 서비스)

#### 주요 기능
- 그룹 생성 및 관리
- 그룹 멤버 관리
- 초대 코드 생성
- Kafka 기반 이벤트 소비/발행

#### 기술 스택
- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL (CQRS 패턴)
- Kafka Producer/Consumer

#### 빌드 및 실행
```bash
cd group

# 개발 환경
./gradlew bootRun

# 운영 환경
./gradlew clean bootJar
java -jar build/libs/group-*.jar
```

---

### 🚪 API Gateway

#### 주요 기능
- 모든 마이크로서비스로의 라우팅
- 인증 검증 (JWT)
- 요청/응답 필터링
- 부하 분산

#### 기술 스택
- Spring Boot 3.5.7
- Spring Cloud Gateway (또는 Zuul)
- JWT 검증

#### 빌드 및 실행
```bash
cd gateway

# 개발 환경
./gradlew bootRun

# 운영 환경
./gradlew clean bootJar
java -jar build/libs/gateway-*.jar
```

---

### 🤖 Emotion AI Service (감정 분석)

#### 주요 기능
- 한국어 감정 분석 (6가지 감정 분류)
- Transformers 기반 모델
- FastAPI 기반 REST API
- OpenAI API 연동 (선택사항)

#### 기술 스택
- FastAPI 0.115+
- Transformers 4.41+
- PyTorch 2.3+
- Pydantic 2.x
- Python 3.9+

#### 설정 및 실행
```bash
cd ai/emotion-ai

# 환경 구성
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 실행
python app.py
# 또는
uvicorn app:app --reload --port 8000 --host 0.0.0.0
```

#### API 엔드포인트
- `POST /v1/emotion/analyze` - 텍스트 감정 분석
- `GET /v1/health` - 헬스 체크

---

### 📹 Video AI Service (실시간 스트리밍)

#### 주요 기능
- WebRTC 기반 실시간 스트리밍
- HLS 녹화 및 재생
- IP 카메라 RTSP 수신
- 저지연 (<2초) 시청

#### 기술 스택
- MediaMTX (RTMP/RTSP/WebRTC/HLS)
- Nginx (프록시)

#### 설정
- `ai/video-ai/mediamtx/mediamtx.yml` - MediaMTX 설정
- `ai/video-ai/nginx/nginx.conf` - Nginx 설정

#### 포트
- WebRTC: 8555
- HLS: 8888
- API: 9997 (내부 전용)

---

### 🌐 Frontend (React + Vite)

#### 주요 기능
- 사용자 인증 UI
- 대시보드 (포트폴리오, 주식 정보)
- 실시간 스트리밍 뷰어
- 그룹 관리 UI

#### 기술 스택
- React 19.0.0
- TypeScript 5.6.0
- Vite 7.0.0
- Tailwind CSS 3.4.1
- Zustand 5.0.8
- React Router 7.9.5
- Axios 1.13.2

#### 프로젝트 구조
```
frontend/
├── src/
│   ├── components/     # 재사용 가능 컴포넌트
│   ├── pages/          # 페이지 컴포넌트
│   ├── layouts/        # 레이아웃
│   ├── api/            # API 클라이언트
│   ├── stores/         # Zustand 스토어
│   ├── types/          # TypeScript 타입 정의
│   ├── lib/            # 유틸리티 함수
│   ├── App.tsx         # 메인 앱 컴포넌트
│   └── main.tsx        # 진입점
├── public/             # 정적 자산
├── package.json
├── vite.config.ts
└── tailwind.config.js
```

#### 빌드 및 실행
```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
# 접속: http://localhost:5173

# 프로덕션 빌드
npm run build
# 결과물: frontend/dist

# 프리뷰
npm run preview

# 린트 검사
npm run lint
```

---

## 데이터베이스 설정

### 🗄️ PostgreSQL 설정

#### 데이터베이스 아키텍처
```
stock_db
├── auth_write/         # Auth 서비스 쓰기 스키마
│   └── auth_user       # 소셜 인증 정보
│   └── outbox          # 이벤트 아웃박스
├── auth_read/          # Auth 서비스 읽기 스키마 (캐시)
│   └── auth_user       # auth_write.auth_user의 복제
├── user_write/         # User 서비스 쓰기 스키마
│   └── user_user       # 사용자 정보
├── user_read/          # User 서비스 읽기 스키마
│   └── user_user
├── group_write/        # Group 서비스 쓰기 스키마
│   ├── group_group     # 그룹 정보
│   └── group_member    # 그룹 멤버
├── group_read/         # Group 서비스 읽기 스키마
│   ├── group_group
│   └── group_member
└── backend_*           # Backend 서비스 스키마
    ├── stocks          # 주식 정보
    ├── portfolios      # 포트폴리오
    ├── trades          # 거래 내역
    ├── news            # 뉴스 데이터
    └── keywords        # 키워드 분석 결과
```

#### 초기화 스크립트
```bash
# 컨테이너 내에서 초기화 스크립트 실행됨
docker-compose exec database psql -U stock_user -d stock_db < init.sql
```

#### CDC 설정 (Debezium)
```bash
# Kafka Connect 커넥터 등록
curl -X POST -H 'Content-Type: application/json' \
  --data @register-connector.json \
  http://localhost:8083/connectors
```

#### 주요 테이블
| 테이블 | 스키마 | 설명 |
|--------|--------|------|
| auth_user | auth_write | 소셜 인증 정보 (Google, Kakao) |
| user_user | user_write | 사용자 기본 정보 |
| group_group | group_write | 그룹 정보 |
| group_member | group_write | 그룹 멤버십 |
| stocks | backend | 주식 마스터 정보 |
| portfolios | backend | 사용자 포트폴리오 |
| trades | backend | 거래 기록 |

---

### 🔄 Kafka 설정

#### Kafka 토픽
```bash
# 자동 생성됨 (docker-compose.yml의 kafka-init 참고)
- User          # 사용자 관련 이벤트
- Order         # 거래/주문 관련 이벤트
```

#### Kafka 클러스터 모드
- **모드**: KRaft (Kafka Raft Consensus)
- **노드 수**: 3 (kafka-1, kafka-2, kafka-3)
- **Replication Factor**: 3
- **Partitions**: 3 (기본값)

#### 모니터링
```bash
# Kafka 토픽 확인
docker-compose exec kafka-1 kafka-topics \
  --bootstrap-server kafka-1:9092 --list

# 토픽 상세 정보
docker-compose exec kafka-1 kafka-topics \
  --bootstrap-server kafka-1:9092 \
  --describe --topic User
```

---

### 💾 MinIO 설정

#### MinIO 버킷
```bash
# 자동 생성됨 (docker-compose.yml의 createbuckets 참고)
- biskit-media    # 미디어 파일 저장 (비디오, 이미지 등)
```

#### 접속 정보
- **UI**: http://localhost:9001
- **사용자명**: minioadmin (기본값)
- **비밀번호**: .env 파일의 MINIO_ROOT_PASSWORD

#### S3 호환 설정
```bash
# AWS CLI를 통한 접근
aws s3api --endpoint-url http://localhost:9000 list-buckets
```

---

## 모니터링 및 로깅

### 📊 Kafka 모니터링

#### Kafka Topics 확인
```bash
# 연결 상태 확인
docker-compose exec kafka-1 \
  kafka-broker-api-versions \
  --bootstrap-server kafka-1:9092

# 컨슈머 그룹 확인
docker-compose exec kafka-1 \
  kafka-consumer-groups \
  --bootstrap-server kafka-1:9092 \
  --list
```

#### Kafka Connect 상태
```bash
# Connect 커넥터 상태 확인
curl http://localhost:8083/connectors

# 특정 커넥터 상세 정보
curl http://localhost:8083/connectors/postgres-cdc

# 커넥터 태스크 상태
curl http://localhost:8083/connectors/postgres-cdc/tasks
```

---

### 📝 로깅 설정

#### Spring Boot 로깅
```yaml
# application.yml
logging:
  level:
    root: INFO
    ilog: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 10
```

#### FastAPI 로깅
```python
# emotion-ai app.py
import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("emotion")
```

#### 로그 파일 위치
```bash
# 컨테이너 내부 로그 확인
docker-compose logs -f gateway
docker-compose logs -f auth
docker-compose logs -f backend
docker-compose logs -f emotion-ai

# 또는 로컬 파일 (마운트된 경우)
tail -f logs/application.log
```

---

## 문제 해결

### 🚨 일반적인 문제

#### 1. Docker 컨테이너 시작 실패
```bash
# 로그 확인
docker-compose logs [service_name]

# 컨테이너 상태 확인
docker-compose ps

# 컨테이너 재시작
docker-compose restart [service_name]

# 특정 서비스만 다시 빌드
docker-compose build --no-cache [service_name]
docker-compose up -d [service_name]
```

#### 2. PostgreSQL 연결 실패
```bash
# PostgreSQL 상태 확인
docker-compose exec database pg_isready -h localhost -U stock_user

# 데이터베이스 연결 테스트
docker-compose exec database psql -U stock_user -d stock_db -c "SELECT 1;"

# 포트 확인
netstat -tulpn | grep 5432
```

#### 3. Kafka 연결 실패
```bash
# Kafka 상태 확인
docker-compose exec kafka-1 kafka-broker-api-versions --bootstrap-server kafka-1:9092

# 토픽 확인
docker-compose exec kafka-1 kafka-topics --bootstrap-server kafka-1:9092 --list

# 컨슈머 그룹 확인
docker-compose exec kafka-1 kafka-consumer-groups --bootstrap-server kafka-1:9092 --list
```

#### 4. Redis 연결 실패
```bash
# Redis 상태 확인
docker-compose exec auth-redis redis-cli ping

# 포트 테스트
telnet localhost 6379
```

#### 5. MinIO 접속 실패
```bash
# MinIO 헬스 체크
curl http://localhost:9000/minio/health/live

# 로그 확인
docker-compose logs minio

# 버킷 확인
docker-compose exec minio mc ls local/
```

#### 6. Kafka Connect CDC 작동 안 함
```bash
# Connect 상태 확인
curl http://localhost:8083/connectors/postgres-cdc/status

# 로그 확인
docker-compose logs connect

# 커넥터 재등록
docker-compose exec connect-init sh -c "curl -X POST -H 'Content-Type: application/json' --data @/config/register-connector.json http://connect:8083/connectors"
```

#### 7. 감정 분석 AI 모델 로딩 실패
```bash
# 모델 디렉토리 확인
docker-compose exec emotion-ai ls -la /app/out_roberta_60cls/checkpoint-6843/

# 로그 확인
docker-compose logs emotion-ai

# 수동 모델 다운로드 및 마운트
# docker-compose.yml에서 volumes 추가
```

#### 8. JWT 토큰 검증 실패
```bash
# JWT 공개키 확인
echo $JWT_ECC_PUBLIC_KEY

# Gateway 로그 확인
docker-compose logs gateway

# Auth 서비스 로그 확인
docker-compose logs auth
```

### 🔧 성능 문제

#### 1. 메모리 부족
```bash
# 메모리 사용량 확인
docker stats

# 컨테이너 메모리 제한 조정 (docker-compose.yml)
services:
  backend:
    mem_limit: 2g
```

#### 2. 디스크 공간 부족
```bash
# 디스크 사용량 확인
df -h

# Docker 이미지 정리
docker system prune -a --volumes

# 로그 정리
docker-compose exec [service] sh -c "rm -rf logs/*"
```

#### 3. 네트워크 지연
```bash
# 네트워크 상태 확인
docker network ls
docker network inspect main

# 서비스 간 통신 테스트
docker-compose exec backend ping gateway
```

#### 4. 데이터베이스 느린 쿼리
```bash
# PostgreSQL 느린 쿼리 로그 활성화
docker-compose exec database psql -U stock_user -d stock_db -c "ALTER SYSTEM SET log_min_duration_statement = 1000;"

# PostgreSQL 재시작
docker-compose restart database

# 인덱스 생성
docker-compose exec database psql -U stock_user -d stock_db -c "CREATE INDEX idx_user_user_id ON user_write.user_user(id);"
```

#### 5. Kafka 처리 지연
```bash
# 컨슈머 랙 확인
docker-compose exec kafka-1 kafka-consumer-groups \
  --bootstrap-server kafka-1:9092 \
  --group [group_id] \
  --describe

# 파티션 수 증가
docker-compose exec kafka-1 kafka-topics \
  --bootstrap-server kafka-1:9092 \
  --alter \
  --topic User \
  --partitions 6
```

---

## 성능 최적화

### ⚡ 애플리케이션 최적화

#### Spring Boot 최적화
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  cache:
    type: redis
    redis:
      time-to-live: 600000
```

#### Redis 최적화
```bash
# Redis 설정 최적화
REDIS_MAXMEMORY_POLICY=allkeys-lru
REDIS_TCP_KEEPALIVE=60
REDIS_TIMEOUT=300
```

#### PostgreSQL 최적화
```sql
-- 연결 풀 설정
ALTER SYSTEM SET max_connections = 200;

-- 메모리 설정
ALTER SYSTEM SET shared_buffers = 256MB;
ALTER SYSTEM SET effective_cache_size = 1GB;
ALTER SYSTEM SET work_mem = 16MB;

-- 인덱스 생성
CREATE INDEX idx_auth_user_provider ON auth_write.auth_user(provider, provider_id);
CREATE INDEX idx_user_email ON user_write.user_user(email);
CREATE INDEX idx_group_member_user ON group_write.group_member(user_id);

-- 설정 적용
SELECT pg_reload_conf();
```

### 🚀 Kafka 최적화

#### Producer 최적화
```properties
# application.yml
spring:
  kafka:
    producer:
      batch-size: 32768
      linger-ms: 10
      acks: 1
      compression-type: snappy
```

#### Consumer 최적화
```properties
spring:
  kafka:
    consumer:
      fetch-min-bytes: 1024
      fetch-max-wait-ms: 500
      max-poll-records: 500
      session-timeout-ms: 30000
```

### 📊 Docker Compose 최적화

#### 리소스 제한
```yaml
services:
  backend:
    mem_limit: 2g
    cpus: '2'
  
  gateway:
    mem_limit: 1g
    cpus: '1'
  
  emotion-ai:
    mem_limit: 4g
    cpus: '2'
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]
```

#### 볼륨 최적화
```yaml
volumes:
  postgres_data:
    driver_opts:
      type: tmpfs
      device: tmpfs
      o: size=2g
```

### 🌐 Nginx 최적화

#### 성능 튜닝
```nginx
# nginx.conf.template
worker_processes auto;
worker_connections 2048;

keepalive_timeout 65;
send_timeout 30;
client_max_body_size 20m;

# 압축
gzip on;
gzip_min_length 1000;
gzip_types text/plain application/json application/javascript;

# 캐싱
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=cache:10m;
proxy_cache_valid 200 10m;
```

### 🎬 AI 모델 최적화

#### FastAPI 비동기 처리
```python
# app.py
from fastapi import FastAPI, BackgroundTasks
import asyncio

@app.post("/v1/emotion/analyze")
async def analyze(text: str):
    # 비동기 처리로 응답 시간 단축
    result = await process_emotion(text)
    return result
```

#### 배치 추론
```python
# Batch 처리로 GPU 효율성 증대
def batch_predict(texts: List[str], batch_size=32):
    results = []
    for i in range(0, len(texts), batch_size):
        batch = texts[i:i+batch_size]
        output = model(batch)
        results.extend(output)
    return results
```

---

## 📞 지원 및 문의

### 🆘 문제 신고
- **이슈 트래커**: GitLab Issues (https://lab.ssafy.com/s13-bigdata-dist-sub1/S13P31A108/-/issues)
- **문서**: 프로젝트 README.md 및 이 manual.md
- **로그**: 각 서비스별 로그 파일 확인

### 📚 추가 자료
- **Spring Boot 공식 문서**: https://spring.io/projects/spring-boot
- **React 공식 문서**: https://react.dev/
- **PostgreSQL 공식 문서**: https://www.postgresql.org/docs/
- **Kafka 공식 문서**: https://kafka.apache.org/documentation/
- **Docker 공식 문서**: https://docs.docker.com/
- **FastAPI 공식 문서**: https://fastapi.tiangolo.com/
- **Nginx 공식 문서**: https://nginx.org/en/docs/

### 🔄 업데이트 및 유지보수
- **정기 업데이트**: 월 1회 (첫째 주)
- **보안 패치**: 즉시 적용
- **성능 모니터링**: 지속적 모니터링
- **의존성 업그레이드**: 분기별 검토

---

## 빠른 시작 가이드

### 개발 환경 구성 (전체 스택)
```bash
# 1. 저장소 클론
git clone https://lab.ssafy.com/s13-bigdata-dist-sub1/S13P31A108.git
cd S13P31A108

# 2. 환경 설정 (.env 파일 생성 - 위 예시 참고)
cat > .env << 'EOF'
[환경 변수 설정]
EOF

# 3. 전체 스택 시작
docker-compose up -d

# 4. 상태 확인
docker-compose ps

# 5. 서비스 접속
# - Frontend: http://localhost (또는 http://localhost:5173 - Vite 개발 서버)
# - Backend API: http://localhost:8080
# - MinIO: http://localhost:9001
# - Kafka Connect: http://localhost:8083
```

### 개발 환경 구성 (프론트엔드만)
```bash
# 1. 로컬 스택 시작 (Docker)
docker-compose up -d database kafka-1 kafka-2 kafka-3 gateway auth backend

# 2. 프론트엔드 실행 (로컬)
cd frontend
npm install
npm run dev

# 3. 접속: http://localhost:5173
```

### 개발 환경 구성 (백엔드만)
```bash
# 1. Docker 기본 스택 시작
docker-compose up -d database kafka-1 kafka-2 kafka-3

# 2. Spring Boot 서비스 실행 (로컬)
cd backend
./gradlew bootRun

# 3. API 테스트: http://localhost:8080
```

---

**📝 문서 버전**: 1.0.0  
**📅 최종 업데이트**: 2025년 11월 24일  
**👥 작성자**: S13P31A108 팀  
**🏢 조직**: SSAFY (Samsung Software AI Academy For Youth)

---

## 라이선스 및 약관
본 프로젝트는 SSAFY의 지적 재산이며, 팀 구성원 외의 무단 배포 및 사용을 금지합니다.
