package com.personal.marketnote.commerce.service.payment.idempotency;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyCreateState;
import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.commerce.exception.IdempotencyKeyInProgressException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.FindPaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.SavePaymentCancelIdempotencyPort;
import com.personal.marketnote.commerce.port.out.payment.idempotency.UpdatePaymentCancelIdempotencyPort;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.metrics.CounterRegistryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * 결제 취소 API 멱등성 오케스트레이터.
 *
 * <p>각 메서드는 비즈니스 TX 와 분리된 {@code REQUIRES_NEW} 로 실행되어, 메인 플로우의 롤백과
 * 무관하게 멱등 상태를 남긴다.
 *
 * <pre>
 *   findOrCreate → (NEW|RETRY)   → 정상 플로우 수행 → markCompleted / markFailed
 *                 → (CACHED)     → 호출자는 캐시 결과 반환 후 플로우 스킵
 *                 → (IN_PROGRESS) → {@link IdempotencyKeyInProgressException} 으로 409 유도
 * </pre>
 */
@Component
@Slf4j
public class PaymentCancelIdempotencyHelper {

    private final FindPaymentPort findPaymentPort;
    private final FindPaymentCancelIdempotencyPort findPaymentCancelIdempotencyPort;
    private final SavePaymentCancelIdempotencyPort savePaymentCancelIdempotencyPort;
    private final UpdatePaymentCancelIdempotencyPort updatePaymentCancelIdempotencyPort;
    private final CounterRegistryHelper counterRegistryHelper;

    public PaymentCancelIdempotencyHelper(
            FindPaymentPort findPaymentPort,
            FindPaymentCancelIdempotencyPort findPaymentCancelIdempotencyPort,
            SavePaymentCancelIdempotencyPort savePaymentCancelIdempotencyPort,
            UpdatePaymentCancelIdempotencyPort updatePaymentCancelIdempotencyPort,
            @Autowired(required = false) @Nullable CounterRegistryHelper counterRegistryHelper
    ) {
        this.findPaymentPort = findPaymentPort;
        this.findPaymentCancelIdempotencyPort = findPaymentCancelIdempotencyPort;
        this.savePaymentCancelIdempotencyPort = savePaymentCancelIdempotencyPort;
        this.updatePaymentCancelIdempotencyPort = updatePaymentCancelIdempotencyPort;
        this.counterRegistryHelper = counterRegistryHelper;
    }

    /**
     * 멱등성 키를 조회하거나 신규로 생성한다.
     *
     * <ul>
     *   <li>COMPLETED 존재 → {@link PaymentCancelIdempotencyResolution.Signal#CACHED}</li>
     *   <li>IN_PROGRESS 존재 → {@link IdempotencyKeyInProgressException} 던짐</li>
     *   <li>FAILED 존재 → retry() 전이 후 {@link PaymentCancelIdempotencyResolution.Signal#RETRY}</li>
     *   <li>없음 → 신규 INSERT 후 {@link PaymentCancelIdempotencyResolution.Signal#NEW}</li>
     * </ul>
     *
     * <p>동시성: 동일 키로 두 요청이 동시에 INSERT 시도 시 UNIQUE 충돌이 발생한다.
     * 충돌 시 재조회로 경합을 해소한다 (loser 는 기존 레코드를 읽어 분기).
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public PaymentCancelIdempotencyResolution findOrCreate(String idempotencyKey, String orderKey, Long buyerId) {
        Optional<PaymentCancelIdempotency> existing =
                findPaymentCancelIdempotencyPort.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return resolveExisting(existing.get());
        }

        Long paymentId = resolvePaymentId(orderKey);

        PaymentCancelIdempotency created = PaymentCancelIdempotency.from(
                PaymentCancelIdempotencyCreateState.builder()
                        .idempotencyKey(idempotencyKey)
                        .paymentId(paymentId)
                        .buyerId(buyerId)
                        .build()
        );

        try {
            savePaymentCancelIdempotencyPort.save(created);
            return PaymentCancelIdempotencyResolution.newlyCreated();
        } catch (DuplicateIdempotencyKeyException dike) {
            log.warn("Idempotency-Key UNIQUE 충돌 — 재조회로 경합 해소. key: {}", idempotencyKey);
            PaymentCancelIdempotency concurrent = findPaymentCancelIdempotencyPort.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> dike);
            return resolveExisting(concurrent);
        }
    }

    /**
     * 비즈니스 TX 커밋 성공 후 멱등성 레코드를 COMPLETED 로 전이한다.
     *
     * <p>레코드가 사라지거나 상태가 IN_PROGRESS 가 아니면 경고 로그만 남긴다.
     * 이미 성공한 비즈니스 TX 를 되돌릴 수는 없고, 동일 키 재요청은 드문 케이스이므로 삼킨다.
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void markCompleted(String idempotencyKey, String resultJson) {
        Optional<PaymentCancelIdempotency> optional =
                findPaymentCancelIdempotencyPort.findByIdempotencyKey(idempotencyKey);
        if (optional.isEmpty()) {
            log.error("COMPLETED 마킹 대상 멱등성 레코드 없음 — 비즈니스 TX 는 커밋된 상태. key: {}", idempotencyKey);
            return;
        }
        PaymentCancelIdempotency idempotency = optional.get();
        if (!idempotency.isInProgress()) {
            log.warn("COMPLETED 마킹 스킵 — 상태가 IN_PROGRESS 아님. key: {}, status: {}",
                    idempotencyKey, idempotency.getStatus());
            return;
        }
        idempotency.complete(resultJson);
        updatePaymentCancelIdempotencyPort.update(idempotency);
    }

    /**
     * 비즈니스 TX 실패 후 멱등성 레코드를 FAILED 로 전이한다.
     *
     * <p>실패 마킹 자체가 예외를 던지면 원본 실패 원인이 가려질 수 있으므로 DB 접근/트랜잭션 실패만 삼킨다.
     *
     * <h3>catch 범위 ({@link DataAccessException}, {@link TransactionException})</h3>
     * <ul>
     *   <li>DB 접근 실패({@link DataAccessException})만 삼킨다 — 원본 비즈니스 실패 원인을 가리지
     *       않기 위함이며, 조회/업데이트 커넥션 장애가 이 경계의 주된 실패 모드다.</li>
     *   <li>{@code InvalidPaymentCancelIdempotencyStatusTransitionException} 은
     *       {@code isInProgress()} 가드로 상태 전이 이전에 이미 차단되어 도달 불가하다.</li>
     *   <li>예기치 못한 {@link RuntimeException} 은 상위로 전파하여 조용히 삼키지 않는다 —
     *       알 수 없는 결함을 감추면 운영 시 디버깅이 불가능해진다.</li>
     *   <li>flush 시점에 메서드 프레임 내부에서 던져지는 {@link TransactionException}
     *       (예: {@code TransactionSystemException}, JPA flush 실패에 수반되는 {@code UnexpectedRollbackException})은
     *       본 catch 로 삼킨다 — {@code update(...)} 호출 중 flush 가 트리거될 수 있고, 이때 예외가 상위로
     *       전파되면 원본 비즈니스 실패 원인이 가려진다. 단, {@code REQUIRES_NEW} 커밋 단계에서 Spring
     *       트랜잭션 AOP 가 프록시 외부로 던지는 {@link TransactionException} 은 메서드 프레임 밖에서
     *       발생하므로 본 catch 로 잡히지 않고 여전히 호출자에게 전파된다.</li>
     * </ul>
     */
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void markFailed(String idempotencyKey, String reason) {
        try {
            Optional<PaymentCancelIdempotency> optional =
                    findPaymentCancelIdempotencyPort.findByIdempotencyKey(idempotencyKey);
            if (optional.isEmpty()) {
                log.warn("FAILED 마킹 대상 멱등성 레코드 없음. key: {}", idempotencyKey);
                return;
            }
            PaymentCancelIdempotency idempotency = optional.get();
            if (!idempotency.isInProgress()) {
                log.warn("FAILED 마킹 스킵 — 상태가 IN_PROGRESS 아님. key: {}, status: {}",
                        idempotencyKey, idempotency.getStatus());
                return;
            }
            idempotency.fail(reason);
            updatePaymentCancelIdempotencyPort.update(idempotency);
        } catch (DataAccessException | TransactionException e) {
            log.error(
                    "멱등성 FAILED 마킹 실패 — type: {}, key: {}, reason: {}",
                    e.getClass().getSimpleName(), idempotencyKey, reason, e
            );
            incrementSwallowedCounter(e);
        }
    }

    private void incrementSwallowedCounter(Exception e) {
        if (FormatValidator.hasNoValue(counterRegistryHelper)) {
            return;
        }
        counterRegistryHelper.increment(
                "idempotency.mark_failed.swallowed.total",
                "멱등성 FAILED 마킹 삼킨 이벤트 총 건수",
                "exception_type", e.getClass().getSimpleName()
        );
    }

    private PaymentCancelIdempotencyResolution resolveExisting(PaymentCancelIdempotency idempotency) {
        if (idempotency.isCompleted()) {
            return PaymentCancelIdempotencyResolution.cached(idempotency.getResult());
        }
        if (idempotency.isInProgress()) {
            throw new IdempotencyKeyInProgressException(idempotency.getIdempotencyKey());
        }
        idempotency.retry();
        updatePaymentCancelIdempotencyPort.update(idempotency);
        return PaymentCancelIdempotencyResolution.retried();
    }

    private Long resolvePaymentId(String orderKey) {
        if (FormatValidator.hasNoValue(orderKey)) {
            throw new PaymentNotFoundException(orderKey);
        }
        UUID orderKeyUuid = UUID.fromString(orderKey);
        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(orderKey));
        return payment.getId();
    }

}
