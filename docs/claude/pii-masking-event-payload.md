# Outbox/Kafka EventEnvelope Payload PII 마스킹 정책

## 1. 배경 (Why)

`EventEnvelope.payload`는 Kafka 토픽으로 발행되어 다운스트림 컨슈머/Outbox 테이블/DLQ/감사 로그 등 여러 영구 저장소에 평문으로 흘러간다. 페이로드 record 필드에 이름/전화번호/주소 등 PII가 평문으로 담기면 다음 결함이 발생한다.

- 컨슈머 측 평문 영구 보관(Outbox/처리 로그/재처리 큐)
- DLQ로 회수된 메시지에 평문 PII 잔존
- 운영자 콘솔/로그 검색에서 평문 노출
- 다른 서비스/테넌트가 동일 PII 값을 키로 cross-link 가능

## 2. 정책

EventEnvelope payload에 들어가는 PII 필드는 **outbox 어댑터(또는 KafkaProducer 어댑터)에서 직렬화 직전에 SHA-256(salt + value)로 단방향 해싱**한다.

- 마스킹 책임자: 발행 어댑터 (consumer는 마스킹된 값만 받는다)
- 알고리즘: SHA-256, lowercase hex (64자)
- Salt: 환경별 또는 테넌트별 분리 (per-tenant unlinkability)
- 마스킹은 record 인스턴스를 새로 만들어 envelope에 전달 (원본 도메인 객체는 그대로)
- 별표 마스킹(`ValueMasker`, `PiiMaskingConverter`)은 **로그 전용**이며 본 정책과 별개

## 3. 사용법

```java
@RequiredArgsConstructor
public class XxxOutboxAdapter implements ... {

    private final Clock clock;
    private final ObjectMapper objectMapper;
    @Value("${event.payload.pii-salt}")
    private final String piiSalt;

    @Override
    public void publish(...) {
        ShippingStatusChangedEvent original = ...;

        // PII 필드만 해시한 새 record 인스턴스 생성
        ShippingStatusChangedEvent masked = new ShippingStatusChangedEvent(
                original.orderId(),
                original.buyerId(),
                original.shippingStatus(),
                original.trackingNumber(),
                original.carrierCode(),
                original.occurredAt(),
                EnvelopePayloadPiiHasher.hash(original.buyerPhoneNumber(), piiSalt),
                EnvelopePayloadPiiHasher.hash(original.buyerName(), piiSalt)
        );

        EventEnvelope<ShippingStatusChangedEvent> envelope = EventEnvelope.of(
                topic, source, masked, clock
        );
        // ... 직렬화 + outbox 저장 / kafka send
    }
}
```

## 4. Salt 관리

### 환경 변수

각 서비스의 `application.yml`에 다음 항목을 추가한다.

```yaml
event:
  payload:
    pii-salt: ${EVENT_PAYLOAD_PII_SALT:dev-salt-change-me}
```

- 운영/QA/로컬 환경별로 서로 다른 salt를 주입한다.
- 운영 salt는 비밀 저장소(예: AWS Secrets Manager, ECS task secret)에서 주입한다.

### SecurityPropertiesValidator 등록 권장

`common/src/main/java/com/personal/marketnote/common/configuration/security/SecurityPropertiesValidator.java`의 `qa.test`/`prod` 프로파일 가드에 `event.payload.pii-salt` 항목을 추가하여, 기본값(`dev-salt-change-me`)으로 운영 환경 부팅 시 앱 시작을 차단한다 (sub-issue로 분리 권장).

### Salt 회전 (Rotation)

- 회전은 이전/신규 salt 기간에 동일 PII가 다른 해시로 발행됨을 의미한다(연결 단절 — 의도된 동작).
- 회전 주기는 별도 운영 정책에서 결정한다 (본 PR 범위 외).

## 5. PII-bearing Events 전수

| Event Record | PII 필드 | 발행 어댑터 | 비고 |
|--------------|---------|------------|------|
| `ShippingStatusChangedEvent` | `buyerName`, `buyerPhoneNumber` | `fulfillment-service/.../ShippingStatusEventOutboxAdapter.java` | Outbox 패턴 |
| `OrderPaymentCompletedEvent` | `recipientName`, `recipientPhoneNumber`, `address`, `addressDetail` | `commerce-service/.../OrderEventKafkaProducer.java` | KafkaProducer 직접 발행 (Outbox 미적용) |
| `UserSignupCompletedEvent` | `userName`, `phoneNumber` | `user-service/.../UserEventKafkaProducer.java` | KafkaProducer 직접 발행 (Outbox 미적용) |
| `ShippingAddressChangedEvent` | `recipientName`, `recipientPhoneNumber`, `address`, `addressDetail` | `user-service/.../ShippingAddressEventKafkaProducer.java` | KafkaProducer 직접 발행 (Outbox 미적용) |

> 위 4건 외 PII 필드 보유 envelope이 추가로 식별되면 본 표에 추가하고 해당 어댑터에 마스킹을 적용한다.

### 식별 절차 (재실행 가능)

```bash
# 1) common/.../kafka/event/*.java record 정의에서 PII 의심 필드(name/phone/address/recipient/buyer*Phone 등) 검색
grep -rn "String.*Name\|phoneNumber\|address\|recipient\|buyer" common/src/main/java/com/personal/marketnote/common/kafka/event/

# 2) 해당 event record의 발행 위치 추적
grep -rln "new <EventClassName>(" --include="*.java" | grep -v test
```

## 6. 신규 어댑터/이벤트 추가 시 체크리스트

- [ ] envelope payload record에 PII 필드(이름/전화/주소/이메일 등)가 포함되는지 확인
- [ ] 포함 시 발행 어댑터에 `EnvelopePayloadPiiHasher.hash(value, piiSalt)` 적용
- [ ] 마스킹된 새 record 인스턴스를 만들어 `EventEnvelope.of(...)`에 전달 (원본 record 직접 전달 금지)
- [ ] 어댑터 단위 테스트에 "PII 필드가 envelope payload에서 64자 hex로 발행된다" 케이스 추가
- [ ] `application.yml`에 `event.payload.pii-salt` 주입 확인 (해당 서비스가 PII 이벤트 발행자인 경우)

## 7. 본 PR의 범위 (#5084)

본 PR은 인프라(Util + 정책 문서 + 식별)만 다룬다. 실제 어댑터 적용은 **어댑터별 sub-issue**로 분리한다 (#5084 본문 참조).

- [x] `EnvelopePayloadPiiHasher` 유틸리티 추가
- [x] 단위 테스트 (deterministic, per-salt unlinkability, hex 길이, UTF-8, salt invariant)
- [x] 본 정책 문서 작성
- [x] PII-bearing event 전수 정리
- [ ] 각 어댑터 적용 (sub-issue)
- [ ] `application.yml` salt 주입 (sub-issue)
- [ ] `SecurityPropertiesValidator` 가드 추가 (sub-issue)
