FROM postgres:16

# 컨테이너 시작 시 실행될 초기화 스크립트를 복사
# /docker-entrypoint-initdb.d/ 경로에 .sql, .sh 파일을 넣어두면
# 데이터베이스가 처음 생성될 때 자동으로 실행됨
COPY init.sql /docker-entrypoint-initdb.d/