package com.personal.marketnote.common.external.log;

import com.personal.marketnote.common.utility.FormatValidator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 외부 시스템 호출 공통 로깅/메트릭 헬퍼.
 *
 * <p>어댑터당 한 줄 위임으로 Micrometer Timer({@code external.call})와 DEBUG/ERROR 로그를
 * 동시에 캡처한다. 호출 전후 latency, operation, tags 메타를 자동으로 기록한다.
 *
 * <ul>
 *   <li>정상 종료: DEBUG 로그 + Timer(outcome=success)</li>
 *   <li>예외 발생: ERROR 로그 + Timer(outcome=failure) + 동일 예외 rethrow (silent fail 금지)</li>
 * </ul>
 *
 * <p>로거는 {@link ExternalSystem#loggerName()}로 분리되어 시스템별 패키지 로거 레벨 토글이 가능하다.
 * MDC의 {@code bizId}/{@code orderKey} 등 enrich 키는 logback 패턴에서 자동 출력되므로 별도 처리 불필요.
 *
 * <p><b>호출자 의무 (CRITICAL — 보안):</b>
 * <ul>
 *   <li>{@code operation}은 컴파일타임 상수만 허용한다 (외부 응답 raw 주입 금지 — 로그 인젝션 방어)</li>
 *   <li>{@code tags} 값은 식별자(orderKey/idempotencyKey/responseCode 등) 한정 — 페이로드 dump 금지</li>
 *   <li>{@code tags}/예외 메시지에 PAN/CVV/카드번호/JWT/OAuth token/API secret/비밀번호 포함 금지.
 *       기존 {@code PiiMaskingConverter} 자동 마스킹은 email/phone/nickname/name 4종만 커버한다 (PCI-DSS Req 3.4)</li>
 *   <li>민감 필드는 호출자가 {@code ValueMasker.mask()} 또는 도메인 VO(예: {@code MaskedCardNumber})를 통해 사전 마스킹</li>
 * </ul>
 */
@Component
public class ExternalCallLogger {

    private static final String METRIC_NAME = "external.call";
    private static final String TAG_SYSTEM = "system";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    private final MeterRegistry meterRegistry;

    public ExternalCallLogger(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 결과를 반환하는 외부 호출을 래핑한다.
     *
     * @param system    외부 시스템 식별자
     * @param operation 호출 오퍼레이션 이름 (예: "approve", "cancel")
     * @param tags      추가 메타 (요청/응답 식별자 등) — null이면 빈 맵으로 처리
     * @param call      외부 호출 supplier
     * @return call의 반환값
     */
    public <T> T record(ExternalSystem system, String operation, Map<String, String> tags, Supplier<T> call) {
        Logger log = LoggerFactory.getLogger(system.loggerName());
        Map<String, String> safeTags = safeTags(tags);
        logStart(log, operation, safeTags);

        long startNanos = System.nanoTime();
        try {
            T result = call.get();
            long elapsedNanos = System.nanoTime() - startNanos;
            recordSuccess(log, system, operation, safeTags, elapsedNanos);
            return result;
        } catch (RuntimeException re) {
            long elapsedNanos = System.nanoTime() - startNanos;
            recordFailure(log, system, operation, safeTags, elapsedNanos, re);
            throw re;
        }
    }

    /**
     * 결과 없는 외부 호출을 래핑한다.
     */
    public void recordVoid(ExternalSystem system, String operation, Map<String, String> tags, Runnable call) {
        record(system, operation, tags, () -> {
            call.run();
            return null;
        });
    }

    /**
     * 민감 정보를 포함할 수 있는 외부 호출을 래핑한다 (실패 시 cause throwable/message 로깅 생략).
     * 예: 토큰 발급, PIN/카드번호 raw 응답 본문 등.
     */
    public <T> T recordSensitive(
            ExternalSystem system, String operation, Map<String, String> tags, Supplier<T> call
    ) {
        Logger log = LoggerFactory.getLogger(system.loggerName());
        Map<String, String> safeTags = safeTags(tags);
        logStart(log, operation, safeTags);

        long startNanos = System.nanoTime();
        try {
            T result = call.get();
            long elapsedNanos = System.nanoTime() - startNanos;
            recordSuccess(log, system, operation, safeTags, elapsedNanos);
            return result;
        } catch (RuntimeException re) {
            long elapsedNanos = System.nanoTime() - startNanos;
            recordFailureSensitive(log, system, operation, safeTags, elapsedNanos, re);
            throw re;
        }
    }

    /**
     * 민감 정보를 포함할 수 있는 결과 없는 외부 호출을 래핑한다 (실패 시 cause throwable/message 로깅 생략).
     */
    public void recordVoidSensitive(
            ExternalSystem system, String operation, Map<String, String> tags, Runnable call
    ) {
        recordSensitive(system, operation, tags, () -> {
            call.run();
            return null;
        });
    }

    private void logStart(Logger log, String operation, Map<String, String> safeTags) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("External call start: operation={}, tags={}", operation, safeTags);
    }

    private void recordSuccess(
            Logger log, ExternalSystem system, String operation,
            Map<String, String> safeTags, long elapsedNanos
    ) {
        timer(system, operation, OUTCOME_SUCCESS).record(elapsedNanos, TimeUnit.NANOSECONDS);
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug(
                "External call success: operation={}, tags={}, elapsedMs={}",
                operation, safeTags, toMillis(elapsedNanos)
        );
    }

    private void recordFailure(
            Logger log, ExternalSystem system, String operation,
            Map<String, String> safeTags, long elapsedNanos, Throwable cause
    ) {
        timer(system, operation, OUTCOME_FAILURE).record(elapsedNanos, TimeUnit.NANOSECONDS);
        log.error(
                "External call failure: operation={}, tags={}, elapsedMs={}, exception={}, message={}",
                operation, safeTags, toMillis(elapsedNanos),
                cause.getClass().getSimpleName(), cause.getMessage(), cause
        );
    }

    private void recordFailureSensitive(
            Logger log, ExternalSystem system, String operation,
            Map<String, String> safeTags, long elapsedNanos, Throwable cause
    ) {
        timer(system, operation, OUTCOME_FAILURE).record(elapsedNanos, TimeUnit.NANOSECONDS);
        log.error(
                "External call failure (sensitive — message/stack 로깅 생략): operation={}, tags={}, elapsedMs={}, exception={}",
                operation, safeTags, toMillis(elapsedNanos), cause.getClass().getSimpleName()
        );
    }

    private Timer timer(ExternalSystem system, String operation, String outcome) {
        return Timer.builder(METRIC_NAME)
                .tag(TAG_SYSTEM, system.metricTag())
                .tag(TAG_OPERATION, operation)
                .tag(TAG_OUTCOME, outcome)
                .register(meterRegistry);
    }

    private Map<String, String> safeTags(Map<String, String> tags) {
        if (FormatValidator.isNull(tags)) {
            return Map.of();
        }
        return tags;
    }

    private long toMillis(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }
}
