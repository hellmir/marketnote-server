# Micrometer 메트릭 네임스페이스 컨벤션

## 목적

Micrometer 메트릭 이름 규칙을 통일하여 후속 도메인 추가 시 네임스페이스 충돌을 방지하고, Prometheus/Grafana 대시보드 쿼리의 일관성을 확보한다.

## 규칙 1 — 네임스페이스 prefix

메트릭이 등록되는 **모듈 위치**를 기준으로 prefix를 결정한다.

| 구분 | 등록 위치 | prefix 형태 | 예시 |
|------|---------|------------|------|
| Cross-cutting / 인프라 | `common/` 모듈 | 일반 prefix (도메인명 없음) | `rate_limit.*`, `sse.*`, `outbox.*`, `kafka.dlt.*` |
| 도메인 특정 | 도메인 서비스 모듈 (`commerce-*`, `reward-*` 등) | `<domain>.<area>.*` | `payment.cancel.*`, `gifticon.pin.*`, `gifticon.sync.*` |

**판별 기준:** 메트릭을 `Counter.builder(...)` 또는 `counterRegistryHelper.increment(...)`로 등록하는 소스 파일이 **어느 모듈에 있는지**로 결정한다.

- `common/` 모듈 소속 → cross-cutting으로 간주, 일반 prefix
- 도메인 서비스 모듈 소속 → 해당 도메인의 prefix 부착

## 규칙 2 — 후미 `.total`

Counter 메트릭은 **누적 카운트** 의미를 명시하기 위해 이름 끝에 `.total`을 부착한다. 본 저장소의 Counter는 예외 없이 적용한다.

- Prometheus 관용(`_total` 접미사)과 정합
- Gauge/Timer는 해당 규칙 대상 아님 (적절한 시점에 도입 시 별도 규정)

## 현재 메트릭 매핑

### Cross-cutting (common 모듈)

| 메트릭 이름 | 등록 위치 | 용도 |
|-------------|---------|------|
| `rate_limit.hit.total` | `common/adapter/in/ratelimit/RateLimitAspect` | Rate Limit 적용 전체 요청 건수 |
| `rate_limit.exceeded.total` | `common/adapter/in/ratelimit/RateLimitAspect` | Rate Limit 초과 거부 건수 |
| `rate_limit.untracked.total` | `common/adapter/in/ratelimit/RateLimitAspect` | Redis increment null 반환 건수 |
| `sse.async.unusable.total` | `common/domain/exception/GlobalExceptionHandler` | SSE 비동기 사용 불가 예외 건수 |
| `outbox.events.failed.total` | `common/outbox/adapter/OutboxMetricsCollector` | Outbox 최대 재시도 초과 FAILED 전환 건수 |
| `outbox.events.resolved.total` | `common/outbox/adapter/OutboxMetricsCollector` | Outbox FAILED 해결 건수 |
| `kafka.dlt.messages.total` | `common/configuration/kafka/DltMetricsCollector` | DLT 적재 메시지 총 건수 |
| `kafka.dlt.reprocess.total` | `common/configuration/kafka/DltMetricsCollector` | DLT 메시지 재처리 건수 |
| `kafka.dlt.resolve.total` | `common/configuration/kafka/DltMetricsCollector` | DLT 메시지 해결 건수 |

### Domain — commerce

| 메트릭 이름 | 등록 위치 | 용도 |
|-------------|---------|------|
| `payment.cancel.idempotency.mark_failed.swallowed.total` | `commerce-application/.../PaymentCancelIdempotencyHelper` | 멱등성 FAILED 마킹 삼킨 이벤트 건수 |
| `payment.cancel.idempotency.serializer.failure.total` | `commerce-application/.../PaymentCancelResultSerializer` | 결제 취소 result 직렬화 실패 건수 |

### Domain — reward

| 메트릭 이름 | 등록 위치 | 용도 |
|-------------|---------|------|
| `gifticon.pin.decrypt.legacy_cbc.rejected.total` | `reward-adapters/.../GifticonPinEncryptAdapter` | GCM1 매직 프리픽스 미존재로 거부된 복호화 건수 |
| `gifticon.sync.invalid_item.total` | `reward-application/.../SyncGifticonGoodsAndBrandsService` | Giftishow 동기화 중 이상치 스킵 건수 |

## 신규 메트릭 추가 체크리스트

1. **등록 위치가 `common/` 모듈인가?** → 일반 prefix 사용 (`rate_limit`, `sse`, `outbox` 등 기존 네임스페이스 확장 또는 새 네임스페이스 신설)
2. **도메인 서비스 모듈인가?** → `<service>.<area>.*` 형식 (예: `payment.cancel.*`, `gifticon.pin.*`)
3. **Counter인가?** → 이름 끝에 `.total` 부착
4. **태그 키는 snake_case**로 작성하고, **태그 값 카디널리티**는 제한된 집합으로만 사용한다 (orderKey/userId 등 고유 식별자는 태그로 부착 금지). **메트릭 이름 segment 내 복합어도 snake_case**(예: `mark_failed`, `invalid_item`, `legacy_cbc`)로 작성한다
5. **설명 문자열(description)**은 한국어 간결 1줄로 작성

## 예외 / 비고

- `sse.async.unusable.total`은 cross-cutting(공통 예외 핸들러 소속)이므로 일반 prefix 유지
- 기존에 `.total`이 누락된 Counter는 발견 즉시 rename 대상
