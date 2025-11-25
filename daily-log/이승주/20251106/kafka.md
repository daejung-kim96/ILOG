🚀 SMT 적용 커넥터 JSON (register-connector.json)
이 내용을 register-connector.json 같은 파일 이름으로 저장하세요.

JSON

{
"name": "outbox-connector",
"config": {
"connector.class": "io.debezium.connector.postgresql.PostgresConnector",
"database.hostname": "database",
"database.port": "5432",

"database.user": "${env:POSTGRES_USER}",
    "database.password": "${env:POSTGRES_PASSWORD}",
"database.dbname": "${env:POSTGRES_DB}",
"database.server.name": "postgres_server",

    "table.include.list": "public.outbox",
    "plugin.name": "pgoutput",

    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",

    "transforms": "unwrap",
    "transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState"

}
}
📋 주요 설정 상세 설명
기본 연결 정보 (database.\*)

"database.hostname": "database": docker-compose.yml에 정의된 PostgreSQL 서비스의 이름입니다.

your_postgres_user, your_postgres_password, your_postgres_db: 이 부분은 docker-compose.yml의 .env 변수에서 사용한 실제 DB 접속 정보로 변경해야 합니다.

"database.server.name": Debezium이 이 DB 소스를 식별하기 위해 사용하는 논리적인 이름입니다. (Kafka 토픽 이름의 일부가 될 수 있음)

Debezium 설정 (table.include.list)

"table.include.list": "public.outbox": [핵심] public 스키마의 outbox 테이블만 감시(CDC)하도록 명시합니다.

컨버터 설정 (converter.\*)

docker-compose.yml의 environment에서 설정한 것과 마찬가지로, 이 커넥터 자체도 스키마를 사용하지 않는 일반 JSON 컨버터를 사용하도록 설정합니다. (schemas.enable": "false")

SMT 설정 (Transforms) - [사용자 요청]

"transforms": "unwrap": 이 커넥터가 unwrap이라는 이름의 변환(Transform)을 사용하도록 선언합니다.

"transforms.unwrap.type": "io.debezium.transforms.ExtractNewRecordState": [가장 중요] unwrap이라는 변환의 실제 동작은 "Debezium의 ExtractNewRecordState 클래스를 사용하라"는 의미입니다. 이 클래스가 바로 Debezium의 복잡한 래퍼(wrapper) JSON에서 after 필드의 값(순수 데이터)만 추출해주는 역할을 합니다.

💡 다음 단계: 이 파일 실행 방법
위 내용을 register-connector.json 파일로 저장합니다.

docker-compose up으로 모든 컨테이너(kafka 3개, connect, database)가 실행 중인지 확인합니다.

터미널에서 connect 컨테이너의 8083 포트로 이 JSON 파일을 POST 방식으로 전송합니다.

Bash

# register-connector.json 파일이 있는 위치에서 실행

curl -X POST -H "Content-Type: application/json" --data @register-connector.json http://localhost:8083/connectors
이 명령어를 실행하면, kafka-connect 컨테이너가 outbox-connector라는 이름의 작업을 시작하고, unwrap SMT가 적용된 채로 outbox 테이블 감시를 시작합니다.

# ===========================

# HELPER: 커넥터 자동 등록용

# ===========================

connect-init:
image: curlimages/curl:latest # curl 명령어를 포함한 경량 이미지
container_name: connect-init
depends_on: - connect # connect 서비스가 시작된 후에 실행
networks: - default # 'connect'와 동일한 네트워크에 있어야 함 (compose가 자동 관리)
volumes: - ./register-connector.json:/config/register-connector.json:ro # 등록할 JSON 파일 마운트 (읽기 전용)
command: >
sh -c "
echo 'Waiting for Kafka Connect (connect:8083) to be ready...'

        until curl -s -f http://connect:8083/connectors; do
          echo 'Connect not ready yet, sleeping 5s...';
          sleep 5;
        done;

        echo 'Kafka Connect is UP. Registering connector...';

        curl -X POST -H 'Content-Type: application/json' \
             --data @/config/register-connector.json \
             http://connect:8083/connectors

        echo 'Connector registration requested. Exiting.';
      "

🚀 "정석"적인 해결책: 토픽은 반드시 "미리" 생성한다
애플리케이션(Connect)이 토픽을 만들게 두는 것이 아니라, **인프라 관리자(개발자)**가 docker-compose up을 실행한 후, 토픽을 직접 생성해야 합니다.

사용 방법: docker-compose up -d로 모든 컨테이너가 실행된 후, 터미널에서 kafka-1 컨테이너에 접속하여 kafka-topics.sh 스크립트를 실행합니다.

1. auth-ms용 토픽 생성 (파티션 30개 / 복제본 3개)

Bash

docker-compose exec kafka-1 kafka-topics.sh \
 --bootstrap-server kafka-1:9092 \
 --create \
 --topic shared_db_server.auth_write_schema.outbox \
 --partitions 30 \
 --replication-factor 3 2. order-ms용 토픽 생성 (파티션 30개 / 복제본 3개)

Bash

docker-compose exec kafka-1 kafka-topics.sh \
 --bootstrap-server kafka-1:9092 \
 --create \
 --topic shared_db_server.order_write_schema.outbox \
 --partitions 30 \
 --replication-factor 3
📋 명령어 해설
docker-compose exec kafka-1: kafka-1 컨테이너 내부에서 명령을 실행합니다.

kafka-topics.sh --create: "새 토픽을 생성하라."

--topic shared_db_server...: [매우 중요] Debezium이 자동으로 생성할 토픽 이름( database.server.name + 스키마명.테이블명 )과 정확히 일치해야 합니다.

--partitions 30: (사용자님의 질문)

"이 토픽을 30개의 조각(파티션)으로 나눈다."

(Scalability): 최대 30개의 컨슈머가 이 토픽을 동시에 병렬 처리할 수 있게 됩니다.

--replication-factor 3: (제가 추가한 필수 옵션)

"이 30개의 파티션 각각을 3개의 복제본(원본 1, 사본 2)으로 만든다."

(High Availability): 3대의 Kafka 노드에 복제본이 분산 저장됩니다. 노드 1~2대가 다운되어도 데이터 유실이 절대 발생하지 않습니다. (이것이 3개 노드를 쓰는 이유입니다)

🤔 파티션 개수는 "몇 개"가 적절한가요?
"30개"는 예시입니다. 파티션 개수를 정하는 기준은 **'컨슈머의 병렬 처리 개수'**와 **'브로커의 부하 분산'**입니다.

컨슈머 병렬성: 컨슈머 그룹은 파티션 개수보다 많은 컨슈머(스레드)를 가질 수 없습니다. 만약 30개의 파티션을 만들면, 최대 30개의 컨슈머가 이 토픽을 병렬로 처리할 수 있습니다.

브로커 부하 분산: 파티션 개수는 브로커 개수(3개)의 배수로 설정하는 것이 가장 좋습니다. (예: 3, 6, 9, 12...)

현업 권장:

시작: partitions=6 (3개 노드 x 2), replication-factor=3

성능 문제 발생 시: 파티션 개수는 나중에 kafka-topics.sh --alter 명령어로 늘릴 수 있습니다. (단, 절대 줄일 수는 없습니다.)

따라서, 처음에는 6이나 9 같은 브로커 배수로 작게 시작하고, 처리량(Throughput)에 따라 늘려가는 것이 정석입니다.
