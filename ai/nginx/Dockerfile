FROM nginx:alpine

# 템플릿과 엔트리포인트 스크립트 복사
COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh

# envsubst, nginx 테스트에 필요한 도구 설치
RUN apk add --no-cache gettext curl \
    && chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
