#!/bin/sh
set -e

# Defaults
: "${HLS_PORT:=8888}"
: "${WEBRTC_PORT:=8889}"
: "${PUBLIC_HOSTNAME:=localhost}"

require() {
  name="$1"
  val="$(eval echo \"\${$1}\")"
  if [ -z "$val" ]; then
    echo "[entrypoint] missing required env: $name" >&2
    exit 1
  fi
}

require STREAM_NAME
require RTSP_URL

echo "[entrypoint] STREAM_NAME=$STREAM_NAME HLS_PORT=$HLS_PORT WEBRTC_PORT=$WEBRTC_PORT PUBLIC_HOSTNAME=$PUBLIC_HOSTNAME"

# Render template (token-based replacement to avoid external deps like envsubst)
sed \
  -e "s|__STREAM_NAME__|$STREAM_NAME|g" \
  -e "s|__RTSP_URL__|$RTSP_URL|g" \
  -e "s|__HLS_PORT__|$HLS_PORT|g" \
  -e "s|__WEBRTC_PORT__|$WEBRTC_PORT|g" \
  -e "s|__PUBLIC_HOSTNAME__|$PUBLIC_HOSTNAME|g" \
  /mediamtx.template.yml > /mediamtx.yml

echo "[entrypoint] generated /mediamtx.yml"

# Exec MediaMTX in foreground (v1.15+ expects config path as arg)
exec /mediamtx /mediamtx.yml
