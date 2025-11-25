# Custom Jenkins Image with Docker and Docker Compose

FROM jenkins/jenkins:lts

USER root

# Docker CLI 설치
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

# Docker Compose 설치
RUN curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

# Node.js 설치 (프론트엔드 빌드용)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g npm@latest

# Gradle 설치 (백엔드 빌드용)
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-8.5-bin.zip && \
    ln -s /opt/gradle/gradle-8.5/bin/gradle /usr/local/bin/gradle && \
    rm /tmp/gradle-8.5-bin.zip

# Jenkins 플러그인 미리 설치
RUN jenkins-plugin-cli --plugins \
    git \
    gitlab-plugin \
    docker-workflow \
    docker-plugin \
    pipeline-stage-view \
    workflow-aggregator \
    ssh-agent

# Jenkins 사용자로 Docker 명령어 실행 가능하도록 설정
RUN usermod -aG docker jenkins

USER jenkins

# Jenkins 초기 설정 스킵 (선택사항)
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
