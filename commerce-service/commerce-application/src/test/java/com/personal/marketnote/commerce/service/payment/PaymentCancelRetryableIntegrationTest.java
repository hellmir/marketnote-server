package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderAmount;
import com.personal.marketnote.commerce.domain.order.OrderAmountCreateState;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentEventStatus;
import com.personal.marketnote.commerce.domain.payment.PaymentSnapshotState;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEventSnapshotState;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.exception.PaymentCancelRetryExhaustedException;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RestoreProductInventoryUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentCancelVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.refund.SaveRefundPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * #4399 — @Retryable + @Transactional(REQUIRES_NEW) + @Recover 조합이 실제 AOP 프록시로 발화되는지
 * 통합 수준에서 검증한다.
 *
 * <p>기존 {@link PaymentCancelTransactionHelperTest}는 Mockito + InjectMocks 기반이라
 * {@code @Retryable} AOP가 발화되지 않는다. 본 테스트는 {@link SpringJUnitConfig} +
 * {@link EnableRetry}로 Spring Context를 띄워 실제 AOP 프록시로 빈을 생성하고,
 * Mock Port가 던지는 {@link OptimisticLockingFailureException}으로
 * 재시도/복구/TX 경계(@Transactional REQUIRES_NEW) 동작을 검증한다.</p>
 *
 * <p>주의: {@code findPaymentPort.findByOrderKey} mock은 매 호출마다 fresh Payment 인스턴스를
 * 반환해야 한다. 동일 인스턴스를 재사용하면 재시도마다 {@code markAsPartiallyRefunded}가 누적되어
 * {@code InvalidRefundAmountException}이 발생하며 이는 {@code retryFor} 목록 밖이라
 * {@code @Recover}가 매칭되지 않는다.</p>
 */
@SpringJUnitConfig
@Import(PaymentCancelRetryableIntegrationTest.TestConfig.class)
@DisplayName("PaymentCancelTransactionHelper @Retryable 통합 테스트")
class PaymentCancelRetryableIntegrationTest {

    private static final Long BUYER_ID = 100L;
    private static final Long ORDER_ID = 1L;
    private static final UUID ORDER_KEY = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();
    private static final String PG_PAYMENT_KEY = "tno_test_123";
    private static final Long PAYMENT_AMOUNT = 50000L;

    @EnableRetry
    @TestConfiguration
    static class TestConfig {

        @Bean
        FindOrderPort findOrderPort() {
            return mock(FindOrderPort.class);
        }

        @Bean
        FindPaymentPort findPaymentPort() {
            return mock(FindPaymentPort.class);
        }

        @Bean
        UpdatePaymentPort updatePaymentPort() {
            return mock(UpdatePaymentPort.class);
        }

        @Bean
        FindPspPaymentEventPort findPspPaymentEventPort() {
            return mock(FindPspPaymentEventPort.class);
        }

        @Bean
        UpdatePspPaymentEventPort updatePspPaymentEventPort() {
            return mock(UpdatePspPaymentEventPort.class);
        }

        @Bean
        RestoreProductInventoryUseCase restoreProductInventoryUseCase() {
            return mock(RestoreProductInventoryUseCase.class);
        }

        @Bean
        ModifyUserPointPort modifyUserPointPort() {
            return mock(ModifyUserPointPort.class);
        }

        @Bean
        SaveRefundPort saveRefundPort() {
            return mock(SaveRefundPort.class);
        }

        @Bean
        FindProductByPricePolicyPort findProductByPricePolicyPort() {
            return mock(FindProductByPricePolicyPort.class);
        }

        @Bean
        PublishPaymentEventPort publishPaymentEventPort() {
            return mock(PublishPaymentEventPort.class);
        }

        @Bean
        FindPaymentAllocationPort findPaymentAllocationPort() {
            return mock(FindPaymentAllocationPort.class);
        }

        @Bean
        FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort() {
            return mock(FindShippingPolicyBySellerIdsPort.class);
        }

        @Bean
        PaymentCancelTransactionHelper paymentCancelTransactionHelper(
                FindOrderPort findOrderPort,
                FindPaymentPort findPaymentPort,
                UpdatePaymentPort updatePaymentPort,
                FindPspPaymentEventPort findPspPaymentEventPort,
                UpdatePspPaymentEventPort updatePspPaymentEventPort,
                RestoreProductInventoryUseCase restoreProductInventoryUseCase,
                ModifyUserPointPort modifyUserPointPort,
                SaveRefundPort saveRefundPort,
                FindProductByPricePolicyPort findProductByPricePolicyPort,
                PublishPaymentEventPort publishPaymentEventPort,
                FindPaymentAllocationPort findPaymentAllocationPort,
                FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort
        ) {
            return new PaymentCancelTransactionHelper(
                    findOrderPort,
                    findPaymentPort,
                    updatePaymentPort,
                    findPspPaymentEventPort,
                    updatePspPaymentEventPort,
                    restoreProductInventoryUseCase,
                    modifyUserPointPort,
                    saveRefundPort,
                    findProductByPricePolicyPort,
                    publishPaymentEventPort,
                    findPaymentAllocationPort,
                    findShippingPolicyBySellerIdsPort
            );
        }
    }

    @Autowired
    private PaymentCancelTransactionHelper txHelper;

    @Autowired
    private FindOrderPort findOrderPort;

    @Autowired
    private FindPaymentPort findPaymentPort;

    @Autowired
    private UpdatePaymentPort updatePaymentPort;

    @Autowired
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Autowired
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;

    @Autowired
    private RestoreProductInventoryUseCase restoreProductInventoryUseCase;

    @Autowired
    private ModifyUserPointPort modifyUserPointPort;

    @Autowired
    private SaveRefundPort saveRefundPort;

    @Autowired
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Autowired
    private PublishPaymentEventPort publishPaymentEventPort;

    @Autowired
    private FindPaymentAllocationPort findPaymentAllocationPort;

    @Autowired
    private FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;

    @BeforeEach
    void setUp() {
        reset(
                findOrderPort, findPaymentPort, updatePaymentPort,
                findPspPaymentEventPort, updatePspPaymentEventPort,
                restoreProductInventoryUseCase, modifyUserPointPort, saveRefundPort,
                findProductByPricePolicyPort, publishPaymentEventPort,
                findPaymentAllocationPort, findShippingPolicyBySellerIdsPort
        );
    }

    @AfterEach
    void tearDown() {
        reset(
                findOrderPort, findPaymentPort, updatePaymentPort,
                findPspPaymentEventPort, updatePspPaymentEventPort,
                restoreProductInventoryUseCase, modifyUserPointPort, saveRefundPort,
                findProductByPricePolicyPort, publishPaymentEventPort,
                findPaymentAllocationPort, findShippingPolicyBySellerIdsPort
        );
    }

    @Test
    @DisplayName("PaymentCancelTransactionHelper는 AOP 프록시로 생성되어 @Retryable이 실제 적용된다")
    void shouldBeAopProxy() {
        assertThat(AopUtils.isAopProxy(txHelper))
                .as("@Retryable 적용을 위해 AOP 프록시여야 한다")
                .isTrue();
    }

    @Test
    @DisplayName("commitPaymentAndPostProcess에 @Transactional(REQUIRES_NEW)가 적용되어 재시도마다 독립 트랜잭션 경계를 가진다")
    void shouldHaveRequiresNewTransactionBoundaryForRetry() throws NoSuchMethodException {
        Method method = PaymentCancelTransactionHelper.class.getDeclaredMethod(
                "commitPaymentAndPostProcess",
                PaymentCancelContext.class,
                PaymentCancelVendorResult.class
        );
        Transactional tx = method.getAnnotation(Transactional.class);
        assertThat(tx)
                .as("commitPaymentAndPostProcess에 @Transactional이 선언되어야 한다")
                .isNotNull();
        assertThat(tx.propagation())
                .as("재시도마다 독립 TX 경계 확보를 위해 propagation=REQUIRES_NEW여야 한다")
                .isEqualTo(Propagation.REQUIRES_NEW);
    }

    @Test
    @DisplayName("@Retryable + @Transactional(REQUIRES_NEW) 조합에서 OptimisticLockingFailureException 발생 시 실제 재시도가 동작하여 최종 커밋 성공한다")
    void shouldRetryAndCommitOnOptimisticLockingFailure() {
        PaymentCancelContext context = buildPartialCancelContext(20000L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

        when(findPaymentPort.findByOrderKey(ORDER_KEY))
                .thenAnswer(invocation -> Optional.of(createPayment(0L)));
        doAnswer(invocation -> {
            throw new OptimisticLockingFailureException("version conflict #1");
        }).doAnswer(invocation -> {
            throw new OptimisticLockingFailureException("version conflict #2");
        }).doAnswer(invocation -> null)
                .when(updatePaymentPort).update(any(Payment.class));

        txHelper.commitPaymentAndPostProcess(context, vendorResult);

        // fresh 재조회 3회 + update 3회 (2회 실패 + 1회 성공)
        verify(findPaymentPort, times(3)).findByOrderKey(ORDER_KEY);
        verify(updatePaymentPort, times(3)).update(any(Payment.class));
    }

    @Test
    @DisplayName("재시도 3회 소진 시 @Recover가 발화하여 PaymentCancelRetryExhaustedException이 전파된다")
    void shouldThrowRetryExhaustedExceptionWhenAllRetriesFail() {
        PaymentCancelContext context = buildPartialCancelContext(20000L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

        when(findPaymentPort.findByOrderKey(ORDER_KEY))
                .thenAnswer(invocation -> Optional.of(createPayment(0L)));
        doAnswer(invocation -> {
            throw new OptimisticLockingFailureException("version conflict");
        }).when(updatePaymentPort).update(any(Payment.class));

        assertThatThrownBy(() -> txHelper.commitPaymentAndPostProcess(context, vendorResult))
                .isInstanceOf(PaymentCancelRetryExhaustedException.class)
                .hasMessageContaining("ERR_PAYMENT_CANCEL_01")
                .hasMessageContaining(ORDER_KEY_STR)
                .hasCauseInstanceOf(OptimisticLockingFailureException.class);

        verify(findPaymentPort, times(3)).findByOrderKey(ORDER_KEY);
        verify(updatePaymentPort, times(3)).update(any(Payment.class));
    }

    @Test
    @DisplayName("재시도 시 TX 롤백으로 Refund INSERT/재고 DB UPDATE/Outbox INSERT가 중복되지 않는다")
    void shouldNotDuplicateSideEffectsOnRetry() {
        PaymentCancelContext context = buildPartialCancelContextWithProducts(20000L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

        when(findPaymentPort.findByOrderKey(ORDER_KEY))
                .thenAnswer(invocation -> Optional.of(createPayment(0L)));
        doAnswer(invocation -> {
            throw new OptimisticLockingFailureException("version conflict #1");
        }).doAnswer(invocation -> {
            throw new OptimisticLockingFailureException("version conflict #2");
        }).doAnswer(invocation -> null)
                .when(updatePaymentPort).update(any(Payment.class));

        txHelper.commitPaymentAndPostProcess(context, vendorResult);

        // 재시도 2회 실패 + 1회 성공 = 총 3회 시도.
        // 실제 운영에서는 @Transactional(REQUIRES_NEW)로 각 시도가 독립 TX이고
        // 실패 시도의 Refund INSERT/재고 UPDATE/Outbox INSERT는 롤백된다.
        // 본 테스트는 실제 TX 롤백을 물리 재현할 수 없으나, 프로덕션 코드 순서
        // (updatePaymentPort.update → saveRefund → publishEvent)를 관찰하면
        // update가 OLFE throw 시 이후 부수효과는 실행되지 않아, 최종 성공 시도(1회)에서만 수행됨을 검증한다.
        //   - findByOrderKey: 3회 (매 시도마다 fresh 재조회)
        //   - update: 3회 (2회 OLFE + 1회 성공)
        //   - saveRefund: 1회 (update 성공 이후에 호출되므로 실패 시도에서는 호출 안 됨)
        //   - publishPaymentCancelledEvent: 1회 (동일)
        //   - restoreInventory: 1회 (동일)
        // 물리적 TX 롤백 검증은 @DataJpaTest 기반 별도 테스트에서 DB @Version 충돌로 확장 가능하다.
        verify(findPaymentPort, times(3)).findByOrderKey(ORDER_KEY);
        verify(updatePaymentPort, times(3)).update(any(Payment.class));
        verify(saveRefundPort, times(1)).save(any(Refund.class));
        verify(publishPaymentEventPort, times(1)).publishPaymentCancelledEvent(
                anyLong(), anyString(), anyLong(), anyLong(), anyLong(), anyLong(),
                any(Boolean.class), any(), any(), any(), any(), any()
        );
        verify(restoreProductInventoryUseCase, times(1))
                .restore(any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("동시 부분취소 2건이 서로 다른 delta로 실행될 때 최종 refundAmount가 두 delta 합과 일치한다")
    void shouldAccumulateRefundAmountUnderConcurrentPartialCancel() throws InterruptedException {
        AtomicLong dbRefundAmount = new AtomicLong(0L);
        AtomicReference<Throwable> thread1Error = new AtomicReference<>();
        AtomicReference<Throwable> thread2Error = new AtomicReference<>();

        // findByOrderKey: 호출 시점 DB 상태 기반 fresh 스냅샷 반환
        when(findPaymentPort.findByOrderKey(ORDER_KEY))
                .thenAnswer(invocation -> Optional.of(createPayment(dbRefundAmount.get())));

        // update: @Version 충돌 시뮬레이션
        //   payment는 markAsPartiallyRefunded(delta) 이후 refundAmount = base + delta 를 담고 있음.
        //   observedBase = requested - delta. observedBase != dbRefundAmount 이면 다른 스레드가 먼저 커밋한 상태.
        doAnswer(invocation -> {
            Payment argPayment = invocation.getArgument(0);
            long requested = argPayment.getRefundAmount().getValue();
            long currentDb = dbRefundAmount.get();
            // delta 후보는 10000, 15000 두 개
            boolean matched10000 = (requested - 10000L) == currentDb;
            boolean matched15000 = (requested - 15000L) == currentDb;
            if (!matched10000 && !matched15000) {
                throw new OptimisticLockingFailureException(
                        "stale base: requested=" + requested + ", currentDb=" + currentDb);
            }
            dbRefundAmount.set(requested);
            return null;
        }).when(updatePaymentPort).update(any(Payment.class));

        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();
        PaymentCancelContext context1 = buildPartialCancelContext(10000L);
        PaymentCancelContext context2 = buildPartialCancelContext(15000L);

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                txHelper.commitPaymentAndPostProcess(context1, vendorResult);
            } catch (Throwable t) {
                thread1Error.set(t);
            } finally {
                doneLatch.countDown();
            }
        });
        executor.submit(() -> {
            readyLatch.countDown();
            try {
                startLatch.await();
                txHelper.commitPaymentAndPostProcess(context2, vendorResult);
            } catch (Throwable t) {
                thread2Error.set(t);
            } finally {
                doneLatch.countDown();
            }
        });

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).as("두 스레드가 10초 내 완료되어야 한다").isTrue();
        assertThat(thread1Error.get())
                .as("스레드1이 재시도 후 최종 성공해야 한다")
                .isNull();
        assertThat(thread2Error.get())
                .as("스레드2가 재시도 후 최종 성공해야 한다")
                .isNull();
        assertThat(dbRefundAmount.get())
                .as("최종 refundAmount는 두 delta 합(10000 + 15000)과 일치해야 한다")
                .isEqualTo(25000L);
        verify(updatePaymentPort, atLeast(2)).update(any(Payment.class));
        verify(findPaymentPort, atLeast(2)).findByOrderKey(ORDER_KEY);
    }

    @Test
    @DisplayName("OptimisticLockingFailureException 이외의 예외는 재시도하지 않고 1회만 실행된 뒤 전파된다")
    void shouldNotRetryOnOtherExceptions() {
        PaymentCancelContext context = buildPartialCancelContext(20000L);
        PaymentCancelVendorResult vendorResult = createSuccessVendorResult();

        when(findPaymentPort.findByOrderKey(ORDER_KEY))
                .thenAnswer(invocation -> Optional.of(createPayment(0L)));
        doAnswer(invocation -> {
            throw new IllegalStateException("non-retryable error");
        }).when(updatePaymentPort).update(any(Payment.class));

        // @Retryable(retryFor = OptimisticLockingFailureException.class) 이므로
        // IllegalStateException은 재시도되지 않고 1회만 수행된다.
        // Spring Retry는 재시도 종료 후 @Recover 매칭을 시도하나
        // IllegalStateException에 매칭되는 @Recover가 없으므로 ExhaustedRetryException으로 래핑된다.
        // 핵심 검증은 "updatePaymentPort.update가 단 1회만 호출되었다"이다.
        assertThatThrownBy(() -> txHelper.commitPaymentAndPostProcess(context, vendorResult))
                .isInstanceOf(org.springframework.retry.ExhaustedRetryException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("non-retryable error");

        verify(updatePaymentPort, times(1)).update(any(Payment.class));
        verify(findPaymentPort, times(1)).findByOrderKey(ORDER_KEY);
    }

    // ---------- 헬퍼 ----------

    private PaymentCancelContext buildPartialCancelContext(Long cancelAmount) {
        Payment payment = createPayment(0L);
        PspPaymentEvent event = createCompleteEvent();
        Order order = createOrder();
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                .cancelAmount(cancelAmount)
                .cancelReason("테스트 부분 취소")
                .build();
        return PaymentCancelContext.of(
                payment, event, order,
                cancelAmount, cancelAmount, PAYMENT_AMOUNT - cancelAmount,
                0L, 0L, false,
                "테스트 부분 취소", "테스트 부분 취소",
                UUID.randomUUID().toString(), PG_PAYMENT_KEY, "STPC",
                command
        );
    }

    private PaymentCancelContext buildPartialCancelContextWithProducts(Long cancelAmount) {
        Payment payment = createPayment(0L);
        PspPaymentEvent event = createCompleteEvent();
        Order order = createOrder();
        CancelPaymentCommand command = CancelPaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .cancelType(CancelPaymentCommand.CancelType.PARTIAL)
                .cancelAmount(cancelAmount)
                .cancelReason("테스트 부분 취소")
                .cancelProducts(List.of(new CancelPaymentCommand.CancelProductItem(100L, 1)))
                .build();
        return PaymentCancelContext.of(
                payment, event, order,
                cancelAmount, cancelAmount, PAYMENT_AMOUNT - cancelAmount,
                0L, 0L, false,
                "테스트 부분 취소", "테스트 부분 취소",
                UUID.randomUUID().toString(), PG_PAYMENT_KEY, "STPC",
                command
        );
    }

    private Payment createPayment(Long refundAmount) {
        PaymentSnapshotState state = PaymentSnapshotState.builder()
                .id(ORDER_ID)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY)
                .pgPaymentKey(PG_PAYMENT_KEY)
                .paymentAmount(PAYMENT_AMOUNT)
                .successYn(true)
                .refundedYn(false)
                .refundAmount(refundAmount)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build();
        return Payment.from(state);
    }

    private PspPaymentEvent createCompleteEvent() {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY_STR)
                .pgCompanyKey("NHN_KCP")
                .pgPaymentKey(PG_PAYMENT_KEY)
                .poStatus(PaymentEventStatus.COMPLETE)
                .method("CARD")
                .amount(PAYMENT_AMOUNT)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .paidAt(LocalDateTime.of(2026, 2, 10, 15, 30))
                .build();
        return PspPaymentEvent.from(state);
    }

    private Order createOrder() {
        OrderProductSnapshotState productState = OrderProductSnapshotState.builder()
                .pricePolicyId(100L)
                .quantity(2)
                .sellerId(10L)
                .unitAmount(25000L)
                .accumulatedPoint(0L)
                .build();
        OrderSnapshotState state = OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.from(OrderAmountCreateState.builder()
                        .totalAmount(PAYMENT_AMOUNT)
                        .paidAmount(null)
                        .couponAmount(null)
                        .pointAmount(0L)
                        .shippingFee(null)
                        .build()))
                .shippingAddress(ShippingAddress.from(ShippingAddressCreateState.builder()
                        .recipientName("수령인")
                        .recipientPhoneNumber("010-1234-5678")
                        .zipCode("12345")
                        .address("서울시 강남구")
                        .addressDetail("상세")
                        .deliveryRequestType(null)
                        .deliveryRequestMessage(null)
                        .build()))
                .orderProductStates(List.of(productState))
                .build();
        return Order.from(state);
    }

    private PaymentCancelVendorResult createSuccessVendorResult() {
        return PaymentCancelVendorResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("취소 성공")
                .amount(String.valueOf(PAYMENT_AMOUNT))
                .rawResponse("{\"res_cd\":\"0000\"}")
                .build();
    }
}
