# Jenkins 배포 가이드

이 폴더에는 EC2에서 Jenkins를 Docker로 실행하기 위한 설정 파일들이 있습니다.

## 📋 파일 구조

```
jenkins/
├── Dockerfile              # Jenkins 커스텀 이미지 (Docker, Node.js, Gradle 포함)
├── docker-compose.yml      # Jenkins 서비스 정의
├── .env                    # 환경 변수 설정
└── README.md              # 이 파일
```

## 🚀 EC2에서 Jenkins 실행하기

### 1️⃣ 프로젝트 클론

```bash
cd /home/ubuntu
git clone https://lab.ssafy.com/your-group/your-repo.git
cd your-repo
```

### 2️⃣ Jenkins 시작

```bash
cd jenkins
docker-compose up -d --build
```

### 3️⃣ 초기 관리자 비밀번호 확인

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 4️⃣ Jenkins 웹 UI 접속

브라우저에서 `http://your-ec2-ip:8090` 접속

### 5️⃣ 초기 설정

1. 위에서 확인한 관리자 비밀번호 입력
2. **Install suggested plugins** 선택
3. 관리자 계정 생성

## 🔧 Jenkins Job 생성

### Pipeline Job 생성

1. **New Item** 클릭
2. Job 이름 입력 (예: `deploy-release`)
3. **Pipeline** 선택
4. **Pipeline** 섹션:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: GitLab 저장소 URL
   - Credentials: GitLab 토큰 추가 필요
   - Branch Specifier: `*/release`
   - Script Path: `Jenkinsfile`

### GitLab Credentials 추가

1. **Manage Jenkins** → **Credentials**
2. **Add Credentials**
   - Kind: `GitLab API token`
   - API token: GitLab에서 발급받은 토큰
   - ID: `gitlab-token`

## 🔗 GitLab Webhook 설정

**GitLab Repository → Settings → Webhooks**

- URL: `http://your-ec2-ip:8090/project/deploy-release`
- Secret Token: Jenkins에서 생성
- Push events: ✅
- Branch filter: `release`

## 📊 Jenkins 상태 확인

```bash
# 로그 확인
docker logs -f jenkins

# 컨테이너 상태
docker ps | grep jenkins

# Jenkins 재시작
docker-compose restart

# Jenkins 중지
docker-compose down

# Jenkins 완전 삭제 (데이터 포함)
docker-compose down -v
```

## 🛠️ 문제 해결

### Docker 권한 오류

```bash
# Docker 소켓 권한 확인
sudo chmod 666 /var/run/docker.sock

# 또는 Jenkins 컨테이너 재시작
docker-compose restart
```

### 네트워크 연결 오류

메인 프로젝트의 Docker 네트워크가 먼저 생성되어 있어야 합니다:

```bash
cd ..
docker-compose up -d
```

## 📝 환경 변수 수정

`.env` 파일을 수정하여 포트 등을 변경할 수 있습니다:

```bash
nano .env
```

수정 후 Jenkins 재시작:

```bash
docker-compose down
docker-compose up -d
```

## 🔄 업그레이드

```bash
docker-compose pull
docker-compose up -d --build
```
