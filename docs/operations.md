# 운영 모니터링

## Kafka DLT Alert

현재 프로젝트는 Grafana Alert Rule, Slack contact point, Notification policy를 provisioning 파일로 관리한다. DLT alert rule은 `qiin-be/grafana/provisioning/alerting/alert-rules.yml`에서 관리한다.

### Metric

Kafka consumer가 모든 retry를 실패한 뒤 `DeadLetterPublishingRecoverer`를 통해 DLT publish에 성공하면 Micrometer Counter가 증가한다.

```text
kafka.dlt.published
```

Prometheus에서는 다음 이름으로 노출된다.

```text
kafka_dlt_published_total
```

예상 label 예시는 다음과 같다.

```text
kafka_dlt_published_total{application="qiin-notification", topic="reservation-event-topic.DLT", consumer="reservation"}
```

### Grafana Alert Rule

Alert name:

```text
Kafka DLT Message Detected
```

PromQL:

```promql
increase(kafka_dlt_published_total{application="qiin-notification", topic="reservation-event-topic.DLT"}[5m]) > 0
```

의미:

```text
최근 5분 동안 새롭게 DLT로 전송된 메시지가 1건 이상 존재하는가
```

권장 설정:

```yaml
condition: 최근 5분 DLT 증가량 > 0
evaluation_interval: 30s
for: 0m
no_data_state: OK
exec_err_state: Alerting
labels:
  component: kafka
  severity: warning
  service: notification-service
annotations:
  summary: Kafka DLT 발생
  description: 최근 5분 동안 새롭게 DLT로 전송된 메시지가 1건 이상 존재합니다.
```

Counter는 누적값이므로 `kafka_dlt_published_total > 0`처럼 직접 비교하지 않는다. 반드시 `increase(...[5m])`로 최근 증가량을 기준으로 alert를 구성한다.

### Slack 알림 연결

Slack contact point는 Grafana provisioning 파일로 관리한다. Slack webhook URL은 repository에 저장하지 않고 Doppler secret으로 관리한 뒤 Grafana 컨테이너 환경변수로 주입한다.

필요한 Doppler secret:

```bash
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
```

적용 위치:

```text
qiin-be/docker-compose.yml
qiin-be/grafana/provisioning/alerting/contact-points.yml
```

Doppler로 compose를 실행하면 Grafana container가 시작될 때 `contact-points.yml`의 `$SLACK_WEBHOOK_URL`이 환경변수 값으로 치환되어 `slack-kafka-alerts` contact point가 생성된다.

```bash
doppler run -- docker-compose up -d grafana
```

Grafana 재시작 후 `Alerting > Contact points`에서 `slack-kafka-alerts`를 확인하고 `Test`로 Slack 수신을 확인한다.

`Alerting > Notification policies`에서 다음 label 기준으로 contact point를 연결한다.

```yaml
matching_labels:
  component: kafka
  severity: warning
  service: notification-service
contact_point: slack-kafka-alerts
```

Slack 알림 메시지 예시는 다음과 같이 구성한다.

```text
[warning] Kafka DLT 발생

service: notification-service
component: kafka
topic: reservation-event-topic.DLT
최근 5분 DLT 발생 건수: {{ $values.A.Value }}
```

Slack 메시지에는 고카디널리티 값인 `reservationId`, `outboxId`, payload, exception message를 label로 넣지 않는다. 필요한 원인 분석 정보는 애플리케이션 로그의 DLT publish 로그에서 확인한다.
