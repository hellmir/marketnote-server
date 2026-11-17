# ShedLock 장기 잡 락 강제 해제 런북

**대상**: ShedLock(#4911) 락이 stuck 상태로 남아 다음 잡 실행이 막힌 경우
**관련 이슈**: #4911 / #4946

---

## 영향 도메인 잡 목록 (lockAtMostFor 기준)

### Long-lived (PT1H 이상) — 강제 해제 우선 검토

| 모듈 | 잡 | lockAtMostFor | 락 이름 (lockProvider) |
|------|----|--------------|---------------------|
| commerce | SettlementScheduler.executeMonthlySettlement | PT2H | commerce-service-settlement (Redis) |
| commerce | ReturnInspectionPollingScheduler.poll | PT2H30M | commerce-service-return-polling (Redis) |
| commerce | AutoConfirmDeliveredOrdersScheduler.run | PT1H | commerce-service-auto-confirm (Redis) |
| reward | GifticonSyncScheduler.sync | PT1H | reward-service-gifticon-sync (Redis) |
| reward | GifticonCouponSyncScheduler.sync | PT1H | reward-service-gifticon-coupon-sync (Redis) |
| fulfillment | FulfillmentStockSyncScheduler.sync | PT1H | fulfillment-service-stock-sync (JDBC) |
| fulfillment | FulfillmentDeliveryStatusPollingScheduler.poll | PT2H30M | fulfillment-service-delivery-polling (JDBC) |
| product | PopularityScheduler.refresh | PT5H | product-service-popularity (Redis) |
| notification | ConsentReminderScheduler.send | PT1H | notification-service-consent-reminder (Redis) |

### Short-lived (PT30M 이하) — 자연 해제 대기 권장

| 모듈 | 잡 | lockAtMostFor |
|------|----|--------------|
| common | OutboxEventCleaner ×2 / OutboxEventPublisher | PT10M |
| common | SagaTimeoutChecker | PT2M30S |
| commerce | PaymentCancelIdempotencyCleanupScheduler | PT30M |
| commerce | InventoryReservationTimeoutChecker | PT5M |
| notification | ExpiredNotificationCleanupScheduler | PT30M |
| notification | ScheduledNotificationPublisher | PT5M |
| notification | AligoMessageResultPollingScheduler | PT25M |
| notification | StaleDeviceTokenCleanupScheduler | PT30M |
| user | ActivateExpiredDeactivationScheduler | PT30M |

---

## 락 강제 해제 절차

### 1. 락 stuck 감지

- Grafana `Scheduler Lock Duration` 패널에서 lockAtMostFor 초과 잡 확인
- Alertmanager `ShedLockStuck` 알람 (lockAtMostFor × 1.2 초과 시 경고)
- 또는 운영자 직접 의심 (다음 cron 시점 도달 후에도 잡 미실행)

### 2. 락 보관 위치 확인

| 모듈 | LockProvider | 보관소 |
|------|-------------|-------|
| commerce, notification, reward, product, user | RedisLockProvider | Redis (Self-host ECS Fargate, AUTH 적용, #4914 정합) |
| fulfillment | JdbcTemplateLockProvider | PostgreSQL `shedlock` 테이블 (#4911 정합) |

### 3-A. Redis 락 강제 해제

**경고**: `RedisLockProvider`의 `unlock` 구현에 **ownership 검증 부재** — 락 소유 노드와 무관한 노드에서 DEL 가능. 강제 해제 시 운영자가 직접 락 보유 노드 종료/완료 여부 사전 확인 필수.

```bash
# 1. ECS task 진입 (또는 Bastion에서 redis-cli)
redis-cli -h "${REDIS_HOST}" -a "${REDIS_PASSWORD}" --no-auth-warning

# 2. ShedLock 키 조회 (job-lock 접두사)
KEYS job-lock:*

# 3. 특정 잡 락 해제 (예: commerce-service-settlement)
DEL job-lock:default:commerce-service-settlement

# 4. 결과 검증
KEYS job-lock:*  # 해당 키 미존재 확인
```

### 3-B. JDBC 락 강제 해제 (fulfillment 전용)

```bash
# fulfillment-service DB 접속 (psql)
psql -h "${DB_HOST}" -U "${DB_USER}" -d fulfillment -c "
  SELECT name, locked_at, locked_until, locked_by
  FROM shedlock
  WHERE locked_until > NOW();
"

# 특정 잡 락 강제 해제 (예: fulfillment-service-delivery-polling)
psql -h "${DB_HOST}" -U "${DB_USER}" -d fulfillment -c "
  DELETE FROM shedlock
  WHERE name = 'fulfillment-service-delivery-polling';
"
```

### 4. 잡 재실행

#### 옵션 A — 다음 cron 대기 (권장)

- 다음 fixedRate/cron 트리거 시점에 자동 실행
- 운영 영향 적음

#### 옵션 B — 수동 트리거 (긴급 시)

각 잡은 별도 수동 트리거 endpoint 미제공. 긴급 재실행 필요 시:

1. Spring Boot actuator `/actuator/scheduledtasks` 로 잡 메타 확인
2. ECS task force-new-deployment → 컨테이너 재시작 시 ShedLock 보유 시점 도달 시 자동 실행
3. 또는 임시로 cron 변경 후 배포 (코드 변경 비추천)

---

## 사전 점검 (강제 해제 직전)

1. **락 보유 노드 상태**: 해당 잡을 실행 중인 ECS task가 정말 종료/실패했는지 CloudWatch Logs로 확인
2. **잡 진행 상황**: 잡이 백엔드 변경(DB 트랜잭션) 중이면 강제 해제 후 데이터 정합성 위험 — 가능하면 자연 만료 대기
3. **알람 사일런스**: 강제 해제 직후 임시 알람 발생 가능 → Alertmanager silence 등록

---

## 사후 검증

- 락 키/레코드 미존재 확인 (3단계)
- 다음 잡 트리거 시점에 정상 실행되는지 모니터링 (Grafana `Scheduler Job Duration` 패널)
- 잡 결과 검증 (도메인별 비즈니스 메트릭, 예: settlement은 PG/seller 정산 합계)

---

## 관련 메모리 / 컨벤션

- `feedback_verify_before_claiming_success.md` — 운영 인프라 변경 후 실배포 검증 필수
- `feedback_serialization_policy_roundtrip_required.md` — 인프라 정책 변경 라운드트립 자동화 테스트
- `project_redis_infra_ecs_fargate.md` — 운영 Redis = ECS Fargate self-host

## 상위 이슈 / 후속

- 부모: #4905 (Prod ECS Fargate 사양 결정 트랙)
- 선행: #4911 (ShedLock 도입), #4944 (PR), #4945 (ArchUnit + 통합 테스트)
- 후속: #4947 (Redis keyPrefix/environment 분리 + lockAtMostFor 강제)
