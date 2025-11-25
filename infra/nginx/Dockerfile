FROM nginx:1.27-alpine

ARG NGINX_ENV=dev
ENV NGINX_ENV=${NGINX_ENV} \
    SERVER_NAME=localhost \
    FRONTEND_UPSTREAM=http://frontend:3000 \
    GATEWAY_UPSTREAM=http://gateway:8080 \
    CLIENT_MAX_BODY_SIZE=20m \
    ERROR_LOG_LEVEL=error \
    SSL_CERT_PATH=/etc/nginx/certs/fullchain.pem \
    SSL_CERT_KEY_PATH=/etc/nginx/certs/privkey.pem

RUN apk add --no-cache bash gettext

COPY entrypoint.sh /docker-entrypoint.d/40-template-entrypoint.sh
COPY templates /etc/nginx/templates

RUN sed -i 's/\r$//' /docker-entrypoint.d/40-template-entrypoint.sh && chmod +x /docker-entrypoint.d/40-template-entrypoint.sh

EXPOSE 80 443
