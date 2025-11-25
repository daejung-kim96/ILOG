🚀 해결 방법: 커넥터 2개 등록하기
Kafka Connect REST API (http://localhost:8083/connectors)에 POST 요청을 두 번 보내야 합니다. 각각 다른 설정 JSON 파일을 사용해서요.

docker-compose.yml은 수정할 필요가 없습니다. connect 서비스는 그대로 두고, 그 안에서 실행될 *작업(task)*을 두 개 등록하는 것입니다.

1. 커넥터 A (첫 번째 DB용): outbox-db1-connector.json
   DB 이름: my_database_1

감시할 테이블:

schema_a.outbox

schema_b.outbox

schema_c.outbox

schema_d.outbox

JSON

{
"name": "outbox-db1-connector",
"config": {
"connector.class": "io.debezium.connector.postgresql.PostgresConnector",

    "database.hostname": "${env:POSTGRES_CONTAINER_NAME}",
    "database.port": "5432",
    "database.user": "${env:POSTGRES_USER}",
    "database.password": "${env:POSTGRES_PASSWORD}",

    "database.dbname": "my_database_1",
    "database.server.name": "server_db1",

    "table.include.list": "schema_a.outbox, schema_b.outbox, schema_c.outbox, schema_d.outbox",

    "plugin.name": "pgoutput",

    // ... (key/value converter 및 SMT 설정은 동일하게) ...
    "transforms": "route",
    "transforms.route.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.route.table.field.event.payload.id": "payload",
    "transforms.route.table.field.event.key": "aggregateId",
    "transforms.route.route.by.field": "aggregateType",
    "transforms.route.table.fields.additional.placement": "eventType:header:X-EventType"

}
} 2. 커넥터 B (두 번째 DB용): outbox-db2-connector.json
DB 이름: my_database_2

감시할 테이블:

schema_e.outbox

schema_f.outbox

schema_g.outbox

schema_h.outbox

JSON

{
"name": "outbox-db2-connector",
"config": {
"connector.class": "io.debezium.connector.postgresql.PostgresConnector",

    "database.hostname": "${env:POSTGRES_CONTAINER_NAME}",
    "database.port": "5432",
    "database.user": "${env:POSTGRES_USER}",
    "database.password": "${env:POSTGRES_PASSWORD}",

    "database.dbname": "my_database_2",
    "database.server.name": "server_db2",

    "table.include.list": "schema_e.outbox, schema_f.outbox, schema_g.outbox, schema_h.outbox",

    "plugin.name": "pgoutput",

    // ... (key/value converter 및 SMT 설정은 동일하게) ...
    "transforms": "route",
    "transforms.route.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.route.table.field.event.payload.id": "payload",
    "transforms.route.table.field.event.key": "aggregateId",
    "transforms.route.route.by.field": "aggregateType",
    "transforms.route.table.fields.additional.placement": "eventType:header:X-EventType"

}
}
💡 핵심 변경 사항
두 JSON 파일에서 반드시 달라야 하는 4가지 설정입니다.

name

커넥터 인스턴스의 고유 이름 (예: outbox-db1-connector, outbox-db2-connector)

database.dbname

연결할 DB 이름 (예: my_database_1, my_database_2)

database.server.name

(매우 중요) Debezium이 내부 토픽을 생성할 때 사용하는 논리적 서버 이름입니다. 두 커넥터 간에 이 값이 반드시 달라야 합니다. (예: server_db1, server_db2)

만약 이 값이 같으면 충돌이 발생합니다.

table.include.list

감시할 outbox 테이블 목록입니다. 콤마(,)로 구분하여 4개의 스키마.테이블을 모두 나열해 주면, 해당 커넥터가 4개 테이블을 모두 감시합니다.

실행 방법
터미널에서 curl을 사용해 kafka-connect 서비스(보통 localhost:8083)에 이 두 개의 JSON 파일을 각각 POST로 전송하면 됩니다.

Bash

# 첫 번째 커넥터 등록

curl -X POST -H "Content-Type: application/json" --data @outbox-db1-connector.json http://localhost:8083/connectors

# 두 번째 커넥터 등록

curl -X POST -H "Content-Type: application/json" --data @outbox-db2-connector.json http://localhost:8083/connectors
이렇게 하면 kafka-connect 서비스 내에서 2개의 Debezium 커넥터가 독립적으로 실행되며, 총 8개의 outbox 테이블을 모두 감시하게 됩니다.
