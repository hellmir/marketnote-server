package com.personal.marketnote.common.saga.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Supplier;

/**
 * SAGA 단계별 진행 상태(시작/완료/실패/보상/보상 실패)를 DEBUG/ERROR 레벨로 캡처하고
 * MDC 에 sagaId/sagaType/sagaStep 을 enrich/cleanup 하는 정적 헬퍼.
 *
 * <p>logger 이름은 {@code com.personal.marketnote.saga} 로 통일되어 운영 환경에서 SAGA 로그만
 * 별도 토글이 가능하다.
 *
 * <p>외부 sagaCompensationFailure MDC flag 는 보상 실패 시 추가 enrich 되며 cleanup 까지 함께 정리된다.
 */
public final class SagaStepLogger {

    private static final Logger log = LoggerFactory.getLogger("com.personal.marketnote.saga");

    private SagaStepLogger() {
    }

    public static void start(SagaContext ctx) {
        putContext(ctx);
        log.debug("SAGA step started. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
        );
    }

    /**
     * 응답 수신 등 step 시작이 아닌 진입 시점에 MDC enrich 만 수행한다.
     * "started" 의미와 분리하여 SagaResponseConsumer 와 같이 이미 종료된 step 의 결과를
     * 처리하는 경로에서 의미 혼동을 방지한다.
     */
    public static void enrich(SagaContext ctx) {
        putContext(ctx);
        log.debug("SAGA response received. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
        );
    }

    public static void complete(SagaContext ctx) {
        log.debug("SAGA step completed. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
        );
        clearContext();
    }

    public static void fail(SagaContext ctx, Throwable cause) {
        log.error("SAGA step failed. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep(), cause
        );
        clearContext();
    }

    /**
     * Throwable 가 아닌 응답 메시지 문자열을 cause 로 캡처해야 할 때 사용하는 오버로드.
     * 마커 예외를 만들 필요 없이 응답 본문을 로그 필드로 직접 출력한다.
     */
    public static void fail(SagaContext ctx, String responseMessage) {
        log.error("SAGA step failed. sagaId={}, sagaType={}, sagaStep={}, response={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep(), responseMessage
        );
        clearContext();
    }

    public static void compensate(SagaContext ctx) {
        putContext(ctx);
        log.debug("SAGA compensation started. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
        );
    }

    public static void compensateFailed(SagaContext ctx, Throwable cause) {
        MDC.put(SagaMdcKeys.SAGA_COMPENSATION_FAILURE, "true");
        log.error("SAGA compensation failed. sagaId={}, sagaType={}, sagaStep={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep(), cause
        );
        clearContext();
    }

    /**
     * Throwable 가 아닌 응답 메시지 문자열을 cause 로 캡처해야 할 때 사용하는 오버로드.
     */
    public static void compensateFailed(SagaContext ctx, String responseMessage) {
        MDC.put(SagaMdcKeys.SAGA_COMPENSATION_FAILURE, "true");
        log.error("SAGA compensation failed. sagaId={}, sagaType={}, sagaStep={}, response={}",
                ctx.sagaId(), ctx.sagaType(), ctx.sagaStep(), responseMessage
        );
        clearContext();
    }

    /**
     * MDC enrich 후 액션 실행, try-finally 로 cleanup 보장.
     * 정상 완료 시 complete 로깅, RuntimeException 발생 시 fail 로깅 후 rethrow.
     *
     * <p>{@link Error} 는 catch 하지 않고 그대로 caller 에 전파되며 fail 로그는 기록되지 않는다.
     * 단 finally 의 MDC cleanup 은 Error 경로에서도 보장된다.
     */
    public static <T> T runWithMdc(SagaContext ctx, Supplier<T> action) {
        putContext(ctx);
        try {
            log.debug("SAGA step started. sagaId={}, sagaType={}, sagaStep={}",
                    ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
            );
            T result = action.get();
            log.debug("SAGA step completed. sagaId={}, sagaType={}, sagaStep={}",
                    ctx.sagaId(), ctx.sagaType(), ctx.sagaStep()
            );
            return result;
        } catch (RuntimeException e) {
            log.error("SAGA step failed. sagaId={}, sagaType={}, sagaStep={}",
                    ctx.sagaId(), ctx.sagaType(), ctx.sagaStep(), e
            );
            throw e;
        } finally {
            clearContext();
        }
    }

    /**
     * SAGA MDC 키만 정리한다 (sagaId/sagaType/sagaStep/sagaCompensationFailure).
     * Consumer 의 finally 블록에서 호출하여 ThreadLocal 누수 방지.
     */
    public static void clear() {
        clearContext();
    }

    private static void putContext(SagaContext ctx) {
        MDC.put(SagaMdcKeys.SAGA_ID, ctx.sagaId());
        MDC.put(SagaMdcKeys.SAGA_TYPE, ctx.sagaType());
        MDC.put(SagaMdcKeys.SAGA_STEP, ctx.sagaStep());
    }

    private static void clearContext() {
        MDC.remove(SagaMdcKeys.SAGA_ID);
        MDC.remove(SagaMdcKeys.SAGA_TYPE);
        MDC.remove(SagaMdcKeys.SAGA_STEP);
        MDC.remove(SagaMdcKeys.SAGA_COMPENSATION_FAILURE);
    }
}
