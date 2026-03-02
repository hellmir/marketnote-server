package com.personal.marketnote.commerce.service.refund;

import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.domain.refund.RefundSnapshotState;
import com.personal.marketnote.commerce.domain.refund.RefundType;
import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;
import com.personal.marketnote.commerce.port.out.refund.FindRefundPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAdminRefundsUseCase 테스트")
class GetAdminRefundsUseCaseTest {

    @InjectMocks
    private GetAdminRefundsService getAdminRefundsService;

    @Mock
    private FindRefundPort findRefundPort;

    @Nested
    @DisplayName("환불 목록 조회 성공")
    class GetRefundsSuccessTest {

        @Test
        @DisplayName("주문 ID로 환불 목록을 정상적으로 조회한다")
        void shouldReturnRefundsByOrderId() {
            // given
            Long orderId = 1L;
            List<Refund> refunds = List.of(
                    createRefund(100L, 1L, orderId, RefundType.FULL_REFUND, 50000L),
                    createRefund(101L, 1L, orderId, RefundType.PARTIAL_REFUND, 20000L)
            );
            when(findRefundPort.findByOrderId(orderId)).thenReturn(refunds);

            // when
            List<GetAdminRefundResult> results = getAdminRefundsService.getRefundsByOrderId(orderId);

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).id()).isEqualTo(100L);
            assertThat(results.get(0).refundType()).isEqualTo(RefundType.FULL_REFUND);
            assertThat(results.get(0).refundAmount()).isEqualTo(50000L);
            assertThat(results.get(1).id()).isEqualTo(101L);
            assertThat(results.get(1).refundType()).isEqualTo(RefundType.PARTIAL_REFUND);
            verify(findRefundPort).findByOrderId(orderId);
        }

        @Test
        @DisplayName("환불이 없는 주문 ID로 조회하면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoRefundsExist() {
            // given
            Long orderId = 999L;
            when(findRefundPort.findByOrderId(orderId)).thenReturn(List.of());

            // when
            List<GetAdminRefundResult> results = getAdminRefundsService.getRefundsByOrderId(orderId);

            // then
            assertThat(results).isEmpty();
            verify(findRefundPort).findByOrderId(orderId);
        }

        @Test
        @DisplayName("환불 결과에 PG 응답 정보가 포함된다")
        void shouldIncludePgResponseInResult() {
            // given
            Long orderId = 1L;
            Refund refund = createRefundWithPgInfo(100L, 1L, orderId);
            when(findRefundPort.findByOrderId(orderId)).thenReturn(List.of(refund));

            // when
            List<GetAdminRefundResult> results = getAdminRefundsService.getRefundsByOrderId(orderId);

            // then
            assertThat(results).hasSize(1);
            GetAdminRefundResult result = results.get(0);
            assertThat(result.pgRefundKey()).isEqualTo("tno_123");
            assertThat(result.pgRawResponse()).isEqualTo("{\"res_cd\":\"0000\"}");
            assertThat(result.processedBy()).isEqualTo("SYSTEM");
            assertThat(result.cancelReason()).isEqualTo("고객 요청");
        }
    }

    @Nested
    @DisplayName("Port 호출 검증")
    class PortInvocationTest {

        @Test
        @DisplayName("findRefundPort.findByOrderId를 정확히 한 번 호출한다")
        void shouldCallFindByOrderIdExactlyOnce() {
            // given
            Long orderId = 1L;
            when(findRefundPort.findByOrderId(orderId)).thenReturn(List.of());

            // when
            getAdminRefundsService.getRefundsByOrderId(orderId);

            // then
            verify(findRefundPort).findByOrderId(orderId);
        }
    }

    private Refund createRefund(Long id, Long paymentId, Long orderId, RefundType type, Long amount) {
        return Refund.from(RefundSnapshotState.builder()
                .id(id)
                .paymentId(paymentId)
                .orderId(orderId)
                .refundType(type)
                .refundAmount(amount)
                .cancelReason("테스트")
                .processedBy("SYSTEM")
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    private Refund createRefundWithPgInfo(Long id, Long paymentId, Long orderId) {
        return Refund.from(RefundSnapshotState.builder()
                .id(id)
                .paymentId(paymentId)
                .orderId(orderId)
                .refundType(RefundType.FULL_REFUND)
                .refundAmount(50000L)
                .cancelReason("고객 요청")
                .processedBy("SYSTEM")
                .pgRefundKey("tno_123")
                .pgRawResponse("{\"res_cd\":\"0000\"}")
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }
}
