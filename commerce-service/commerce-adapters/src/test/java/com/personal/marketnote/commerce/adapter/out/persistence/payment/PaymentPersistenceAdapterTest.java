package com.personal.marketnote.commerce.adapter.out.persistence.payment;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.repository.PaymentJpaRepository;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.payment.PaymentSnapshotState;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, PaymentPersistenceAdapter.class})
@DisplayName("PaymentPersistenceAdapter 통합 테스트")
class PaymentPersistenceAdapterTest {

    @Autowired
    private PaymentPersistenceAdapter adapter;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private static final UUID ORDER_KEY = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        paymentJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Payment createAndSavePayment(Long orderId, UUID orderKey, Long paymentAmount) {
        Payment payment = Payment.from(
                PaymentCreateState.builder()
                        .orderId(orderId)
                        .orderKey(orderKey)
                        .paymentAmount(paymentAmount)
                        .build()
        );
        return adapter.save(payment);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("결제를 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSavePaymentAndReturnWithId() {
            // when
            Payment saved = createAndSavePayment(1L, ORDER_KEY, 50000L);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getOrderId()).isEqualTo(1L);
            assertThat(saved.getOrderKey()).isEqualTo(ORDER_KEY);
            assertThat(saved.getPaymentAmount().getValue()).isEqualTo(50000L);
            assertThat(saved.getRefundedYn()).isFalse();
            assertThat(saved.getRefundAmount().getValue()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("주문 ID로 결제를 조회한다")
        void shouldFindByOrderId() {
            // given
            createAndSavePayment(1L, ORDER_KEY, 50000L);

            // when
            Optional<Payment> found = adapter.findByOrderId(1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderId()).isEqualTo(1L);
            assertThat(found.get().getPaymentAmount().getValue()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Payment> found = adapter.findByOrderId(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderKey")
    class FindByOrderKey {

        @Test
        @DisplayName("주문 키로 결제를 조회한다")
        void shouldFindByOrderKey() {
            // given
            createAndSavePayment(1L, ORDER_KEY, 50000L);

            // when
            Optional<Payment> found = adapter.findByOrderKey(ORDER_KEY);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderKey()).isEqualTo(ORDER_KEY);
        }

        @Test
        @DisplayName("존재하지 않는 주문 키로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<Payment> found = adapter.findByOrderKey(UUID.randomUUID());

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("결제 성공 상태로 업데이트한다")
        void shouldUpdatePaymentSuccess() {
            // given
            Payment saved = createAndSavePayment(1L, ORDER_KEY, 50000L);
            entityManager.flush();
            entityManager.clear();

            Payment toUpdate = Payment.from(
                    PaymentSnapshotState.builder()
                            .id(saved.getId())
                            .orderId(1L)
                            .orderKey(ORDER_KEY)
                            .pgPaymentKey("PG_KEY_001")
                            .paymentAmount(50000L)
                            .successYn(true)
                            .refundedYn(false)
                            .refundAmount(0L)
                            .build()
            );

            // when
            adapter.update(toUpdate);
            entityManager.flush();
            entityManager.clear();

            // then
            Payment found = adapter.findByOrderKey(ORDER_KEY).orElseThrow();
            assertThat(found.getPgPaymentKey()).isEqualTo("PG_KEY_001");
            assertThat(found.getSuccessYn()).isTrue();
        }

        @Test
        @DisplayName("환불 상태로 업데이트한다")
        void shouldUpdatePaymentRefund() {
            // given
            Payment saved = createAndSavePayment(1L, ORDER_KEY, 50000L);
            entityManager.flush();
            entityManager.clear();

            Payment toUpdate = Payment.from(
                    PaymentSnapshotState.builder()
                            .id(saved.getId())
                            .orderId(1L)
                            .orderKey(ORDER_KEY)
                            .pgPaymentKey("PG_KEY_001")
                            .paymentAmount(50000L)
                            .successYn(true)
                            .refundedYn(true)
                            .refundAmount(50000L)
                            .build()
            );

            // when
            adapter.update(toUpdate);
            entityManager.flush();
            entityManager.clear();

            // then
            Payment found = adapter.findByOrderKey(ORDER_KEY).orElseThrow();
            assertThat(found.getRefundedYn()).isTrue();
            assertThat(found.getRefundAmount().getValue()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("존재하지 않는 결제를 업데이트하면 DomainNotFoundException이 발생한다")
        void shouldThrowWhenPaymentNotFound() {
            // given
            Payment nonExistent = Payment.from(
                    PaymentSnapshotState.builder()
                            .id(999L)
                            .orderId(999L)
                            .orderKey(UUID.randomUUID())
                            .paymentAmount(50000L)
                            .successYn(false)
                            .refundedYn(false)
                            .refundAmount(0L)
                            .build()
            );

            // when & then
            assertThatThrownBy(() -> adapter.update(nonExistent))
                    .isInstanceOf(DomainNotFoundException.class);
        }
    }
}
