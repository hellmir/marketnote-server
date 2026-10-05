package com.personal.marketnote.commerce.adapter.out.persistence.refund;

import com.personal.marketnote.commerce.adapter.out.persistence.refund.repository.RefundJpaRepository;
import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundCreateState;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.common.configuration.AuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, RefundPersistenceAdapter.class})
@DisplayName("RefundPersistenceAdapter 통합 테스트")
class RefundPersistenceAdapterTest {

    @Autowired
    private RefundPersistenceAdapter adapter;

    @Autowired
    private RefundJpaRepository refundJpaRepository;

    @BeforeEach
    void setUp() {
        refundJpaRepository.deleteAll();
    }

    private Refund createAndSaveRefund(Long paymentId, Long orderId, RefundType refundType, Long refundAmount) {
        Refund refund = Refund.from(
                RefundCreateState.builder()
                        .paymentId(paymentId)
                        .orderId(orderId)
                        .refundType(refundType)
                        .refundAmount(refundAmount)
                        .cancelReason("테스트 환불 사유")
                        .processedBy("SYSTEM")
                        .pgRefundKey("PG_REFUND_KEY")
                        .pgRawResponse("{\"result\":\"success\"}")
                        .build()
        );
        return adapter.save(refund);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("환불을 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveRefundAndReturnWithId() {
            // when
            Refund saved = createAndSaveRefund(1L, 100L, RefundType.FULL_REFUND, 50000L);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getPaymentId()).isEqualTo(1L);
            assertThat(saved.getOrderId()).isEqualTo(100L);
            assertThat(saved.getRefundType()).isEqualTo(RefundType.FULL_REFUND);
            assertThat(saved.getRefundAmount().getValue()).isEqualTo(50000L);
            assertThat(saved.getCancelReason()).isEqualTo("테스트 환불 사유");
            assertThat(saved.getProcessedBy()).isEqualTo("SYSTEM");
            assertThat(saved.getPgRefundKey()).isEqualTo("PG_REFUND_KEY");
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("주문 ID로 환불 목록을 최신순으로 조회한다")
        void shouldFindByOrderIdOrderByCreatedAtDesc() {
            // given
            createAndSaveRefund(1L, 100L, RefundType.PARTIAL_REFUND, 10000L);
            createAndSaveRefund(1L, 100L, RefundType.PARTIAL_REFUND, 20000L);
            createAndSaveRefund(2L, 200L, RefundType.FULL_REFUND, 50000L);

            // when
            List<Refund> refunds = adapter.findByOrderId(100L);

            // then
            assertThat(refunds).hasSize(2);
            assertThat(refunds).allMatch(r -> r.getOrderId().equals(100L));
        }

        @Test
        @DisplayName("환불이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoRefunds() {
            // when
            List<Refund> refunds = adapter.findByOrderId(999L);

            // then
            assertThat(refunds).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPaymentId")
    class FindByPaymentId {

        @Test
        @DisplayName("결제 ID로 환불 목록을 최신순으로 조회한다")
        void shouldFindByPaymentIdOrderByCreatedAtDesc() {
            // given
            createAndSaveRefund(1L, 100L, RefundType.PARTIAL_REFUND, 10000L);
            createAndSaveRefund(1L, 200L, RefundType.PARTIAL_REFUND, 20000L);
            createAndSaveRefund(2L, 300L, RefundType.FULL_REFUND, 50000L);

            // when
            List<Refund> refunds = adapter.findByPaymentId(1L);

            // then
            assertThat(refunds).hasSize(2);
            assertThat(refunds).allMatch(r -> r.getPaymentId().equals(1L));
        }

        @Test
        @DisplayName("환불이 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoRefunds() {
            // when
            List<Refund> refunds = adapter.findByPaymentId(999L);

            // then
            assertThat(refunds).isEmpty();
        }
    }
}
