# Stage 1: pull official MediaMTX binary
FROM bluenviron/mediamtx:latest AS mediamtx

# Stage 2: lightweight shell environment so entrypoint.sh can run
FROM alpine:3.20

# Copy MediaMTX binary from the official image
COPY --from=mediamtx /mediamtx /mediamtx

# Copy entrypoint and config template
COPY --chmod=+x entrypoint.sh /entrypoint.sh
COPY mediamtx.template.yml /mediamtx.template.yml

# Expose media ports
EXPOSE 8888 8889

ENTRYPOINT ["/entrypoint.sh"]
