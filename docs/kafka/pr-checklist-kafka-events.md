# PR 리뷰 체크리스트 — Kafka 이벤트 스키마 변경

이벤트 페이로드(`common/src/main/java/com/personal/marketnote/common/kafka/event/` 하위) 또는 Consumer 역직렬화/매핑 로직을 변경하는 PR 은 본 체크리스트를 PR 본문 하단에 복사해 채운다.

상세 근거는 [`event-schema-evolution-policy.md`](./event-schema-evolution-policy.md) 참조.

## 스키마 변경 여부

- [ ] 본 PR 은 Kafka 이벤트 페이로드 스키마를 변경한다
- [ ] 변경 대상 이벤트:
  - (예: `PricePolicyCreatedEvent`)

## 변경 유형 분류

해당하는 항목에 모두 체크.

- [ ] Optional 필드 추가
- [ ] Required 필드 추가 (→ 3단계 배포 SOP 필수)
- [ ] 필드 제거
- [ ] 필드 이름 변경 (→ 원칙적 금지, "제거 + 추가"로 분해)
- [ ] 필드 타입 변경 (→ 원칙적 금지, 새 필드 병행 전략)
- [ ] 중첩 record 구조 변경
- [ ] enum 값 추가/제거/이름 변경
- [ ] 변경 없음 (Consumer 로직만 수정)

## 구 스키마 메시지 호환성 검토

- [ ] 구 스키마 메시지가 신 Consumer 에 들어올 때 null 이 되는 필드를 모두 열거했다
- [ ] 각 null 필드에 대해 `fail-closed`(DLT 유입) / `fail-open`(기본값 대체) / `ack-skip`(저장 스킵 후 ack) 정책을 결정했다
- [ ] NOT NULL DB 제약으로 전파되는 필드가 있다면 ReadModel/Entity 매핑 단계에서 가드를 추가했다
- [ ] enum 값 미지(unknown) 가능성이 있는 필드에 `@JsonEnumDefaultValue` 또는 UNKNOWN 분기를 추가했다

## Consumer null 가드

- [ ] Consumer 측 null 가드 코드가 본 PR 또는 선행 PR 에 포함됐다
- [ ] 관련 선행 PR 링크:
- [ ] 단위 테스트로 구 스키마(null 필드) 입력 경로를 검증했다
- [ ] `common/src/test/java/com/personal/marketnote/common/kafka/event/KafkaMessageSerializationSchemaCompatibilityTest` 에 해당 이벤트의 forward/backward compatibility 케이스를 추가했다

## 롤링 배포 순서

- [ ] 배포 순서: Consumer 먼저 / Producer 먼저 / 동시 (해당 항목 명시)
- [ ] 선택 근거를 PR 본문에 기재했다
- [ ] 발행자/소비자가 다른 서비스에 위치하는 경우, 양측 배포 완료 전까지 Consumer null 가드를 유지할 계획이다

## 백필 여부 (Required 필드 추가 시)

- [ ] 구 스키마로 발행된 이벤트에 대한 백필 전략을 명시했다
- [ ] 전략: Outbox 재발행 / ReadModel 직접 패치 / 백필 불필요 (해당 항목 명시)
- [ ] 재발행 시 Consumer 측 멱등 처리 여부를 확인했다

## DLT 재처리 영향

- [ ] 현재 DLT 에 대기 중인 해당 이벤트 타입 메시지의 처리 계획이 있다 (재처리 / 수동 교정 / 영구 폐기)
- [ ] 재처리 시 본 PR 의 Consumer 가드가 해당 메시지를 성공적으로 처리함을 확인했다

## 문서/이슈

- [ ] 이벤트 record 의 신규 필드에 `@since` javadoc 주석을 추가했다
- [ ] 필수 필드 추가의 경우 3단계 배포 이슈(단계 1/2/3)로 분할 발행됐다
- [ ] PR 본문에 이벤트 스키마 변경 요약(이전 → 이후)을 기재했다
