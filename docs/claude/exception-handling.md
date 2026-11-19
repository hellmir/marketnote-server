# 예외 처리 표준 — catch 블록 stack trace 보존 패턴

본 문서는 `try / catch` 블록에서 **원인(cause) stack trace 를 잃지 않도록** 강제하는 표준 가이드다. 자동 룰 도입은 ArchUnit API 한계로 보류되었으며(상세는 하단 "ArchUnit 자동 검증" 섹션 참조), 본 가이드는 코드 리뷰 + 분류별 일괄 보강 sub-issue (#4890, #4971~#4974, #4999~#5002) 로 강제한다.

## 분류 체계

| 분류 | 상황 | 핵심 규칙 |
|------|------|----------|
| A | 도메인 예외 변환 | `throw new DomainException(message, cause)` — cause 보존 필수 |
| B | ERROR 로그 후 rethrow | `log.error("...", cause)` — 마지막 인자 Throwable 필수 |
| C | 외부 API fallback | `log.warn("fallback applied: ...", cause)` — Throwable 인자 + fallback 사실 명시 |
| D | 의도된 무시 | `catch (SpecificException ignored)` — 변수명 통일 + 사유 주석 1줄 필수 |
| E | `@Async` / `@Scheduled` 비동기 진입점 (boundary) | 호출자 전파 안 됨 → ERROR + cause 인자 필수 |

## 분류 A — 도메인 예외 변환 (cause 보존)

### 규칙

- `try { ... } catch (E e) { throw new DomainException(...); }` 형태로 변환할 때 반드시 **cause 를 생성자에 전달**한다.
- 도메인 예외 생성자에 `Throwable cause` 시그니처를 보강한다 (없으면 추가).
- 로그는 호출 지점에서 직접 찍지 않고 `GlobalExceptionHandler` 위임을 원칙으로 한다.

### 예시

```java
// WRONG: cause 누락 → stack trace 끊김
try {
    repository.save(entity);
} catch (DataIntegrityViolationException dive) {
    throw new DuplicateLedgerTransactionException(transaction.getIdempotencyKey());
}

// CORRECT: cause 전파 → 원인 추적 가능
try {
    repository.save(entity);
} catch (DataIntegrityViolationException dive) {
    throw new DuplicateLedgerTransactionException(transaction.getIdempotencyKey(), dive);
}
```

```java
// 도메인 예외에 Throwable cause 시그니처 추가 (메시지 포맷은 기존 클래스 유지)
public class DuplicateLedgerTransactionException extends RuntimeException {
    public DuplicateLedgerTransactionException(String idempotencyKey) {
        super("이미 처리된 장부 거래입니다. 멱등성 키: " + idempotencyKey);
    }
    public DuplicateLedgerTransactionException(String idempotencyKey, Throwable cause) {
        super("이미 처리된 장부 거래입니다. 멱등성 키: " + idempotencyKey, cause);
    }
}
```

본 가이드의 보강 범위는 **cause 시그니처 추가** 만이다. 메시지 포맷 정합화 (`ERR_*` prefix 도입 등) 는 별도 작업으로 분리한다.

## 분류 B — ERROR 로그 후 rethrow

### 규칙

- 호출자에 예외를 던지는 동시에 ERROR 로그를 남겨야 하는 경우, **`log.error(message, cause)` 의 마지막 인자에 반드시 `Throwable` 을 전달**한다.
- 메시지에는 컨텍스트 식별자(주문 ID, 사용자 ID 등)를 포함한다.
- 단순 메시지만 찍는 `log.error("...")` 형태는 stack trace 가 누락되므로 금지.

### 예시

```java
// WRONG: stack trace 누락
try {
    paymentClient.confirm(orderKey);
} catch (PaymentClientException pce) {
    log.error("결제 확정 실패. orderKey=" + orderKey);
    throw pce;
}

// WRONG: getMessage 만 찍음 → stack 손실
log.error("결제 확정 실패: " + pce.getMessage());

// CORRECT: cause 인자 전달
try {
    paymentClient.confirm(orderKey);
} catch (PaymentClientException pce) {
    log.error("결제 확정 실패. orderKey={}", orderKey, pce);
    throw pce;
}
```

## 분류 C — 외부 API fallback (대체 동작 수행)

### 규칙

- 외부 API 호출 실패 시 fallback (대체 응답, 캐시 반환, 빈 결과 반환 등) 을 적용하는 경우 **WARN 레벨**로 로그를 남기고, **cause 를 마지막 인자로 전달**한다.
- 메시지에 "fallback applied" 또는 그에 준하는 명시적 표현을 포함하여 후행 분석 시 패턴 grep 이 가능해야 한다.
- 프로젝트의 `ExternalCallLogger` (`common/external/log/ExternalCallLogger.java`) 가 적용 가능한 외부 호출은 헬퍼를 우선 사용한다 — 이미 KCP/Aligo/Fassto/OAuth/S3 어댑터에 적용된 표준 패턴.

### 예시 — 우선순위 1: ExternalCallLogger 헬퍼 사용

```java
// CORRECT: ExternalCallLogger 가 ERROR 로그 + Timer + rethrow 까지 자동 처리
return externalCallLogger.record(
        ExternalSystem.KCP, "approve", Map.of("orderKey", orderKey),
        () -> kcpClient.approve(request)
);
```

### 예시 — 우선순위 2: 헬퍼 미적용 영역의 raw fallback 패턴

```java
// WRONG: WARN 누락 + cause 손실
try {
    return externalClient.fetch(id);
} catch (ExternalCallException ece) {
    return Optional.empty();
}

// CORRECT: WARN + cause 전달 + fallback 사실 명시
try {
    return externalClient.fetch(id);
} catch (ExternalCallException ece) {
    log.warn("fallback applied: external fetch failed, returning empty. id={}", id, ece);
    return Optional.empty();
}
```

## 분류 D — 의도된 무시 (silent ignore)

### 규칙

- 의도적으로 예외를 무시할 때만 `catch (SpecificException ignored)` 형태를 사용한다.
- **변수명은 반드시 `ignored`** 로 통일 (린터/리뷰어 식별 용이).
- **catch 블록 첫 줄에 무시 사유 주석 1줄을 반드시 추가**한다.
- 일반 `Exception` / `Throwable` / `RuntimeException` 무시는 금지 (구체 예외 타입만 허용). 광역 예외 무시는 silent fail 의 대표 패턴.
- 빈 catch 블록(`catch (E e) { }` 변수명 무관) 금지.

### 예시

```java
// WRONG: 일반 Exception 무시 + 사유 없음
try {
    cleanup();
} catch (Exception ignored) {
}

// WRONG: 빈 블록
try {
    cleanup();
} catch (IOException e) {
}

// CORRECT: 구체 예외 + 사유 주석
try {
    cleanup();
} catch (NoSuchFileException ignored) {
    // 이미 삭제된 임시 파일 — 멱등 cleanup 이므로 무시
}
```

## 분류 E — `@Async` / `@Scheduled` boundary

### 규칙

- `@Async` / `@Scheduled` / Kafka `@KafkaListener` 등 **비동기 진입점 (boundary)** 에서 발생한 예외는 호출자 스레드로 전파되지 않는다 — 반드시 boundary 에서 catch + ERROR 로그 + cause 인자 전달.
- DLQ / retry / alert 가 필요한 경우 별도 도메인 정책에 따라 처리.
- `@Async` 메서드는 `private` 금지, self-invocation 금지, 반환 타입은 `void` 또는 `CompletableFuture<T>` 만. 상세는 `~/.claude/rules/coding-style.md` "@Async 메서드 규칙" 참조.

### 예시

```java
// CORRECT: @Async boundary 에서 cause 보존
@Async
public CompletableFuture<Void> sendNotificationAsync(NotificationCommand command) {
    try {
        notificationPort.send(command);
        return CompletableFuture.completedFuture(null);
    } catch (NotificationDispatchException nde) {
        log.error("async notification dispatch failed. userId={}", command.userId(), nde);
        return CompletableFuture.failedFuture(nde);
    }
}
```

## 금지 패턴 요약

| 패턴 | 사유 |
|------|------|
| `catch (E e) { }` (빈 블록) | silent fail — stack 완전 손실 |
| `catch (Exception ignored)` / `catch (Throwable ignored)` | 광역 예외 무시는 시스템 결함 은폐 |
| `log.error("msg")` (cause 누락) | stack trace 손실 |
| `log.error("msg: " + e.getMessage())` | message 만 남고 stack 손실 |
| catch 블록 내부에서 `throw new DomainException(...)` 형태로 cause 인자를 누락 | 원인 예외 체인 끊김 |

## ArchUnit 자동 검증 (sub-issue 위임)

본 가이드의 catch 블록 본문 규칙(빈 catch / `log.error` Throwable 인자 / `ignored` 변수명) 은 **ArchUnit 으로 검증할 수 없다**. ArchUnit (1.3.0 기준) 의 `TryCatchBlock` API 는 catch 된 예외 타입(`getCaughtThrowables`) 과 try 블록 내부 access 만 노출하며, **catch 블록 본문의 access / 변수명은 노출하지 않기 때문**이다.

ArchUnit 으로 검증 가능한 한 가지는 **광역 예외 catch (`Exception` / `Throwable` / `RuntimeException`) 자체의 사용 금지**다. 그러나 정당한 boundary catch (`@Async`, top-level handler, 외부 API fallback) 가 다수 존재 (코드베이스 grep 기준 비동기 boundary 약 136건, 외부 API fallback 다수) 하여 광범위한 화이트리스트가 필요하고, 그 결과 룰의 실효성이 떨어진다.

따라서 본 PR(#4930) 에서는 가이드 문서만 도입하고, 자동 룰 / 일괄 보강은 다음 sub-issue 에서 분류별로 진행한다:

- #4890 (common 46건), #4971 (commerce 140건), #4972 (reward 108건), #4973 (fulfillment 108건), #4974 (notification 25건), #4999 (product 7건), #5000 (user 10건), #5001 (community 2건), #5002 (file 1건) — 분류 A~E 별 catch 블록 보강
- fulfillment client 의 `catch (Exception ignored)` 4건 (`FulfillmentReturnDeliveryClient` 597/685/701줄, `FulfillmentDirectReturnDeliveryClient` 366/382줄) 은 #4973 보강 시 구체 예외 타입 (`IOException` / `RestClientException` 등) 으로 좁힘 시도, 불가 시 정당화 주석 추가
- 보강 진행 시점에 ArchUnit 외 정적 분석 도구(SonarQube `java:S2139` / `java:S2095` / `java:S108`, ErrorProne `EmptyCatch`) 도입 검토
