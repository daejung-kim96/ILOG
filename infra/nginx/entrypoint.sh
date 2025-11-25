#!/bin/sh
set -euo pipefail

template_file="/etc/nginx/templates/nginx.${NGINX_ENV:-dev}.conf"
target_file="/etc/nginx/conf.d/default.conf"

if [ ! -f "$template_file" ]; then
  echo "[nginx-template] Missing template: $template_file" >&2
  exit 1
fi

echo "[nginx-template] Rendering $(basename "$template_file") -> $target_file"

envsubst '$$SERVER_NAME $$FRONTEND_UPSTREAM $$GATEWAY_UPSTREAM $$CLIENT_MAX_BODY_SIZE $$SSL_CERT_PATH $$SSL_CERT_KEY_PATH $$ERROR_LOG_LEVEL' \
  < "$template_file" > "$target_file"
