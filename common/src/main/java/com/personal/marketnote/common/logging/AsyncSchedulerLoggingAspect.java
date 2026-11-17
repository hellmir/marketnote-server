package com.personal.marketnote.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * {@code @Async} / {@code @Scheduled} 메서드의 진입/완료/예외를 DEBUG 레벨로 캡처하는 AOP aspect.
 *
 * <p>prod(com.personal=INFO)에서는 isDebugEnabled() false → 분기 미진입, 영향 0.
 * QA(com.personal=DEBUG)에서만 entry/exit/exception + timing 출력.
 *
 * <p>한계: Spring AOP 프록시 특성상 같은 클래스 내 self-invocation(this.method())은
 * 어드바이스가 적용되지 않는다. 외부 빈에서 호출되는 진입점만 로깅된다.
 */
@Aspect
@Component
public class AsyncSchedulerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AsyncSchedulerLoggingAspect.class);

    @Around("@annotation(org.springframework.scheduling.annotation.Async) "
            + "|| @annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object logAsyncOrScheduled(ProceedingJoinPoint pjp) throws Throwable {
        if (!log.isDebugEnabled()) {
            return pjp.proceed();
        }
        String name = pjp.getSignature().toShortString();
        long startNanos = System.nanoTime();
        log.debug("Enter: {}", name);
        try {
            Object result = pjp.proceed();
            log.debug(
                    "Exit: {} (took {} ms)",
                    name,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos)
            );
            return result;
        } catch (Throwable t) {
            log.debug(
                    "Exception in {} (took {} ms): cause={}",
                    name,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos),
                    t.getClass().getSimpleName(),
                    t
            );
            throw t;
        }
    }
}
