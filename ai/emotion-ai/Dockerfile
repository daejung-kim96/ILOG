# Base
FROM python:3.10-slim

# 빠른 빌드를 위한 기본 패키지
RUN apt-get update && apt-get install -y --no-install-recommends \
    git curl && \
    rm -rf /var/lib/apt/lists/*

# Workdir
WORKDIR /app

# Python deps
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

COPY app.py .

# 환경변수
ENV MODEL_DIR=/app/out_roberta_60cls
ENV MODEL_VERSION=60cls@local
ENV TEMPERATURE=1.0
ENV TOKENIZERS_PARALLELISM=false

EXPOSE 8001

# 런 커맨드
CMD ["uvicorn","app:app","--host","0.0.0.0","--port","8001","--workers","1"]
