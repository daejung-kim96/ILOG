#!/bin/sh
set -euo pipefail

# 환경 변수 치환 후 nginx.conf 생성
envsubst '${NGINX_SERVER_NAME} ${SSL_CERT_PATH} ${SSL_CERT_KEY_PATH}' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf

# 설정 검증
nginx -t

# nginx 실행
exec nginx -g 'daemon off;'
