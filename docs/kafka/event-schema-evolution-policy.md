# Kafka 이벤트 스키마 진화 호환성 정책

본 문서는 `common/src/main/java/com/personal/marketnote/common/kafka/event/` 에 정의된 Kafka 이벤트 페이로드의 변경(진화) 시 호환성을 유지하기 위한 원칙과 표준 절차(SOP)를 정의한다.

## 1. 배경과 목적

### 배경 (#4555, 2026-10-18 회귀)

`PricePolicyCreatedEvent` 페이로드에 `productName`, `brandName`, `productTags`, `selectedOptions` 등 필드가 확장됐다. 확장 전에 발행된 구 스키마 이벤트가 이후 배포된 신 Consumer에서 역직렬화되면서 새 필드들이 모두 `null` 로 매핑됐고, `ReadModel`(`product_catalog_read_models`) 저장 시 `name NOT NULL` 제약 위반으로 DLT 유입이 발생했다.

### 목적

- 신/구 Producer 와 Consumer 가 동시에 공존하는 롤링 배포 구간에서도 이벤트 처리가 안전하게 동작하도록 보장한다.
- 페이로드 변경이 있을 때 "구 메시지가 소비자에 들어왔을 때 어떻게 동작하는가" 를 PR 단계에서 명시적으로 검토한다.
- DLT 에 적재된 구 스키마 메시지의 재처리 판단 기준을 명확히 한다.

## 2. 호환성 수준 정의

| 수준 | 의미 | 상황 |
|------|------|------|
| Backward | 신 Consumer 가 구 Producer 의 이벤트를 읽을 수 있다 | Consumer 선배포 시 필요 |
| Forward | 구 Consumer 가 신 Producer 의 이벤트를 읽을 수 있다 | Producer 선배포 시 필요 |
| Full | Backward + Forward 동시 충족 | 롤링 배포 중 발행자/소비자 순서가 혼재할 때 필요 |

**팀 기본값:** `Full` 을 지향한다. 모든 페이로드 변경은 구 메시지 소비와 신 메시지 소비가 **한 Consumer 내에서 공존 가능**해야 한다.

팀 기본값의 근거:
- 현재 배포 파이프라인은 서비스별 롤링 배포이며, Producer/Consumer 의 배포 순서를 강제하지 않는다.
- 스키마 레지스트리(Confluent Schema Registry 등) 미도입 상태이므로 런타임에서 스키마 강제 검증이 불가능하며, Spring Kafka `JsonDeserializer` 기본 ObjectMapper 가 알 수 없는 필드를 무시하는 동작에 의존한다. 이 동작을 정책적으로 고정하려면 `KafkaConsumerConfig` 에 명시 설정(`FAIL_ON_UNKNOWN_PROPERTIES=false`)을 추가하는 것이 안전하며, 별도 후속 과제로 둔다.
- DLT 재처리 시점에도 구 스키마 메시지가 유입될 수 있으므로, Consumer 는 항상 양방향 호환을 가정해야 한다.

## 3. 변경 유형별 정책

이벤트 클래스는 Java `record` 로 구현되며 Jackson 으로 직렬화된다. 역직렬화는 Spring Kafka `JsonDeserializer` 기본 ObjectMapper 를 사용하며, 알 수 없는 필드는 무시되는 기본 동작에 의존한다(자세한 내용은 2장 참조).

| 변경 유형 | 허용 여부 | 요구 조건 / 주의 사항 |
|-----------|-----------|----------------------|
| **Optional 필드 추가** | 허용 | Consumer 가 `null` 을 허용해야 한다. NOT NULL DB 제약으로 전파되지 않도록 ReadModel/Entity 매핑 단계에서 기본값/가드 필수. 가드 방식 용어: **fail-closed**(필수 필드 null 시 DLT 전송), **fail-open**(기본값/공집합 대체 후 저장). 4장 SOP 참조 |
| **Required(필수) 필드 추가** | 직접 추가 금지 | 4장 "필수 필드 추가 SOP" 를 따라 3단계 배포 필수 |
| **필드 제거** | 직접 제거 금지 | 소비자가 해당 필드를 참조하지 않음을 전 서비스에서 확인 → `@Deprecated` 마킹 + 충분 drain 구간(소비 완료까지 대기 시간) → 제거. 제거 PR 은 별도 이슈로 분리 |
| **필드 이름 변경** | 금지 | 이름 변경은 "제거 + 추가" 2단계 변경으로 강제 분해. 한 번에 변경 시 신/구 양쪽에서 `null` 유입 |
| **필드 타입 변경**(primitive → object, Long → String 등) | 원칙적 금지 | 불가피한 경우 새 필드를 추가한 뒤 구 필드를 deprecated 로 유지, 모든 Consumer 가 새 필드를 읽도록 전환된 후에 구 필드 제거 |
| **중첩 record 필드 추가**(예: `ProductTagSnapshot` 내부 필드 추가) | 허용(Optional 필드 추가와 동일) | Consumer 매핑 단계에서 `null` 허용 보장 |
| **중첩 record 구조 재배치**(flat → nested 변경 등) | 금지 | 타입 변경과 동일하게 취급, 새 필드 병행 유지 후 단계적 전환 |
| **컬렉션 필드**(`List<T>`) **추가** | 허용 | Consumer 는 `null` 과 `empty list` 를 동일하게 취급해야 함. `FormatValidator.hasNoValue` 로 가드 |
| **enum 값 추가** | 허용 | Consumer 는 미지의 enum 값을 만나도 역직렬화 실패하지 않도록 `@JsonEnumDefaultValue` 또는 별도 UNKNOWN 분기 필요 |
| **enum 값 제거** | 금지 | 구 메시지에 남아 있는 값이 역직렬화 실패를 유발. 제거 대신 `@Deprecated` 처리 후 장기 drain(소비 완료까지 대기) |
| **enum 값 이름 변경** | 금지 | 필드 이름 변경과 동일 사유 |
| **EventEnvelope 자체 구조 변경**(`eventId`/`eventType`/`source`/`timestamp`/`payload`) | 원칙적 금지 | 변경 시 모든 서비스의 역직렬화 코드가 영향을 받음. 변경이 필요하면 별도 상위 이슈로 전사 대응 |

## 4. 필수 필드 추가 SOP (3단계 배포)

필수(NOT NULL) 성격의 필드를 추가해야 하는 경우, 아래 3단계로 진행한다.

### 단계 1 — Optional 필드로 추가 + Consumer null 가드 배포

- 이벤트 record 에 필드를 추가한다. 타입은 박싱(`Long`, `String` 등)을 사용해 `null` 수용.
- Consumer 측 매핑/ReadModel 저장 로직에 **null 가드**를 선배포한다.
  - **fail-closed**: 필수 비즈니스 필드가 null 이면 DLT 로 보낸다 (예: `productId` 등 식별자).
  - **fail-open**: 보조 표현 필드가 null 이면 기본값/공집합으로 대체해 ReadModel 저장을 성공시킨다 (예: `productTags`, `selectedOptions`).
  - **ack-skip**: 구 스키마로 판단되지만 이미 다른 경로로 반영된 경우 저장을 스킵하고 ack 한다 (예: #4555 의 `PricePolicyCreatedEvent` `productName` null — ReadModel 은 기존 레코드 유지, 재발행으로 복구).
- 판단 기준: "해당 필드가 null 일 때 비즈니스 정합성이 깨지는가" → 깨지면 fail-closed. "이미 다른 경로로 반영됐는가" → 반영됐으면 ack-skip. 그 외 → fail-open.
- Producer 는 아직 배포하지 않는다.

### 단계 2 — Producer 배포 + 백필

- Producer 를 배포해 이후 발행되는 이벤트에는 새 필드가 채워진다.
- 단계 1 이전에 발행됐던 구 메시지에 대해서는 백필이 필요하다. 두 가지 전략 중 선택:
  - **Outbox 재발행**: Outbox 테이블에서 해당 이벤트 타입의 미처리/처리완료 레코드를 새 스키마로 재생성해 발행. 재발행 이벤트는 Consumer 측 멱등 처리(예: `EventEnvelope.eventId` 기반 dedup 저장소 또는 도메인 자체 멱등 키)가 확보되어야 한다. 공통 dedup 인프라는 별도 후속 과제.
  - **읽기 모델 직접 패치**: 이벤트 재발행이 side-effect(재고 차감 등)를 유발할 가능성이 있는 경우, ReadModel/보강 테이블을 소스 테이블에서 직접 UPDATE 한다.
- 두 전략의 선택 기준: 이벤트가 "사실 통지(fact notification)" 이면 재발행 가능, "명령/전이(command)" 이면 직접 패치.

### 단계 3 — Optional → 정책상 필수로 승격

- 단계 2 의 Producer 배포 완료 + 백필 완료를 기준으로 한다. 이후 발행 이벤트에 대해 Consumer 경고 로그(`productName null 가드` 등)가 미발생하는 것을 관찰한 후 승격한다. 필드 단위 null 카운트 메트릭은 별도 과제.
- 승격은 코드 레벨의 타입 변경이 아니라 **문서/컨벤션** 레벨이다. Record 필드의 Java 타입은 박싱 유지(신규 구독자를 위한 Forward 호환 확보).
- 승격 시점에 Consumer 의 null 가드는 "방어용 최후 안전망" 으로 유지한다. 제거하지 않는다.

## 5. 롤링 배포 중 버전 불일치 구간 대응

### 5.1 배포 순서 선택

| 상황 | 권장 순서 |
|------|-----------|
| 새 **필수** 필드 추가 (4장 SOP 포함) | Consumer 먼저 → Producer 나중 |
| Optional 필드 추가, Producer 만 확장 | Consumer 먼저(null 허용 가드) → Producer |
| 필드 제거(deprecation 후 최종 삭제) | Consumer 먼저(해당 필드 참조 제거) → Producer |
| 이벤트 타입 자체 신설 | Consumer 먼저(토픽 구독 등록) → Producer |

원칙: **Consumer 가 항상 "더 관대"해야 한다.** 먼저 배포된 Consumer 는 구 Producer 의 메시지도, 신 Producer 의 메시지도 처리할 수 있어야 한다.

### 5.2 버전 불일치 허용 구간

- 발행자/소비자 양측 배포가 모두 완료될 때까지 Consumer null 가드를 반드시 유지한다.
- 배포 파이프라인 소요 시간은 운영 환경에 따라 다르며, Jenkins 파이프라인 실측값은 운영 Runbook 에 별도 관리한다.

### 5.3 canary / blue-green 상황

- Canary 로 Producer 의 일부만 새 스키마를 발행하는 구간에서는 기존 Consumer group offset 을 재설정하지 않는다. Consumer 는 mixed-traffic(구/신 스키마 이벤트 혼재 상태)을 그대로 처리한다.
- Blue-green 전환 시 Consumer offset 은 blue → green 으로 이어받는다. Offset reset 은 **절대 자동화하지 않는다.** reset 시 구 스키마 메시지가 대량 재유입될 수 있다.

## 6. DLT 재처리 정책

DLT(Dead Letter Topic) 는 역직렬화 실패, 비즈니스 예외, NOT NULL 제약 위반 등으로 Consumer 가 처리하지 못한 메시지가 적재되는 토픽이다.

### 6.1 재처리 전 사전 확인

1. DLT 메시지의 `eventType` 과 발행 시점 확인. `EventEnvelope.timestamp` 로 해당 메시지가 어느 스키마 버전에서 발행됐는지 추정.
2. 해당 `eventType` 에 페이로드 변경이 있었는지 확인 (CHANGELOG 또는 git log 기준).
3. **스키마 변경이 원인인 경우**: 현재 Consumer 가 구 스키마에 대한 null 가드를 갖추고 있는지 확인. 없으면 가드 보강 후 재처리.
4. **비즈니스 예외인 경우**: 데이터 정합성 문제 원인 제거 후 재처리.

### 6.2 재처리 판단 매트릭스

| DLT 유입 원인 | 현재 Consumer 상태 | 조치 |
|---------------|-------------------|------|
| 구 스키마 null 필드 → NOT NULL 제약 | null 가드 있음 | 재처리 가능 |
| 구 스키마 null 필드 → NOT NULL 제약 | null 가드 없음 | 가드 배포 후 재처리 |
| enum 값 제거로 역직렬화 실패 | UNKNOWN 분기 있음 | 재처리 가능 |
| enum 값 제거로 역직렬화 실패 | UNKNOWN 분기 없음 | 수동 교정(DB 직접 보정) 또는 영구 폐기 |
| 필드 이름 변경으로 null | - | 수동 교정 필수(Consumer 로직만으로 복구 불가) |
| 비즈니스 예외(중복, 상태 위반) | - | 원인 제거 후 재처리 |

### 6.3 영구 폐기 vs 수동 교정 기준

- 영구 폐기(acknowledge 후 삭제) 기준:
  - 메시지가 이미 다른 경로로 실질 반영됨 (멱등성 키로 중복 확인).
  - 비즈니스상 재발행 가능한 이벤트이며 원본이 여전히 존재.
- 수동 교정 기준:
  - 이벤트가 "사실 통지" 성격이고 원본 소스가 DB 에 존재 → 원본 기준으로 ReadModel 수동 UPDATE.
  - 이벤트가 "명령/전이" 성격이고 재발행이 side-effect 를 일으킴 → 상태 머신 기준으로 최종 상태만 수동 교정.

## 7. 문서화/네이밍 규칙

### 7.1 이벤트 클래스 javadoc

페이로드에 필드를 추가할 때 해당 필드에 `@since` 주석을 남긴다. 이벤트 스키마는 별도 버전 번호가 없으므로 변경 날짜(`YYYY-MM-DD 변경 목적`)를 사용한다.

```java
public record PricePolicyCreatedEvent(
        Long productId,
        Long pricePolicyId,
        Long sellerId,
        /** @since 2026-10-18 ReadModel 보강용 */
        String productName,
        /** @since 2026-10-18 ReadModel 보강용 */
        String brandName,
        ...
) {}
```

### 7.2 CHANGELOG 기록

이벤트 스키마 변경은 PR 본문의 Reason/Action 및 이슈 본문에 요약한다. 별도 changelog 파일은 필요 시 후속 이슈로 발행한다.

### 7.3 필수 필드 추가 시 이슈 발행

필수 필드 추가(4장 SOP 대상)는 다음 3개 서브 이슈로 분할 발행한다.

1. `[Feature] {이벤트명} Optional 필드 추가 + Consumer null 가드` — 단계 1
2. `[Feature] {이벤트명} Producer 확장 + 구 메시지 백필` — 단계 2
3. `[Task] {이벤트명} 필드 필수화 문서 승격` — 단계 3 (문서 레벨)

## 8. PR 리뷰 체크리스트 연계

이벤트 페이로드 변경이 포함된 PR 은 `docs/kafka/pr-checklist-kafka-events.md` 체크리스트를 PR 본문에 복사해 채워야 한다.

## 9. 현행 이벤트 목록 (참고)

**작성 시점:** 2026-10-31. 최신 목록은 `common/src/main/java/com/personal/marketnote/common/kafka/event/` 디렉토리를 단일 출처로 본다. 본 장은 정책 설명용 예시.

`common/src/main/java/com/personal/marketnote/common/kafka/event/` 하위 이벤트는 모두 본 정책을 적용한다. 대표 이벤트:

- `EventEnvelope<T>` (공통 봉투) — 구조 변경 금지 대상
- `PricePolicyCreatedEvent`, `PricePolicyDeletedEvent` — #4555 사례 대상
- `ProductRegisteredEvent`, `ProductUpdatedEvent`, `ProductDeletedEvent`
- `OrderPaymentCompletedEvent`, `OrderCancelledEvent`, `OrderReturnedEvent`, `OrderPurchaseConfirmedEvent`
- `PaymentApprovedEvent`, `PaymentCancelledEvent`, `PaymentFailedEvent`
- `StockReservedEvent`, `StockReleasedEvent`, `InventoryChangedEvent`
- `SettlementExecutedEvent`, `PointAccruedEvent`, `UserReferralCompletedEvent`
- `ShippingAddressChangedEvent`, `ShippingPolicyChangedEvent`, `ShippingStatusChangedEvent`
- `FulfillmentGoodsSyncedEvent`, `FulfillmentInventorySyncedEvent`, `FulfillmentDeliveryWorkStatusChangedEvent`
- `NoticeRegisteredEvent`, `InquiryAnsweredEvent`, `ReviewRegisteredEvent`, `ReviewUpdatedEvent`, `ReviewDeletedEvent`, `ReviewReplyRegisteredEvent`
- `PushNotificationSentEvent`, `UserSignupCompletedEvent`, `EventRegisteredEvent`, `ImageChangedEvent`, `ProductTagChangedEvent`
- `ReturnRequestedEvent`, `ReturnRejectedEvent`, `ReturnInspectionCompletedEvent`
- `OrderAutoConfirmFailedEvent`, `OrderCancelFailedEvent`
