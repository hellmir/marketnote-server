package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.OrderProductNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOrderProductUseCaseTest {

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private UpdateOrderProductPort updateOrderProductPort;

    @InjectMocks
    private UpdateOrderProductService updateOrderProductService;

    @Test
    @DisplayName("주문 상품의 리뷰 상태를 true로 업데이트하면 정상적으로 반영된다")
    void updateReviewStatus_withIsReviewedTrue_updatesSuccessfully() {
        // given
        Long orderId = 1L;
        Long pricePolicyId = 100L;
        OrderProduct orderProduct = buildOrderProduct(orderId, pricePolicyId, false);
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, true);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

        // when
        updateOrderProductService.updateReviewStatus(command);

        // then
        ArgumentCaptor<OrderProduct> captor = ArgumentCaptor.forClass(OrderProduct.class);
        verify(updateOrderProductPort).update(captor.capture());

        OrderProduct captured = captor.getValue();
        assertThat(captured.getIsReviewed()).isTrue();
        assertThat(captured.getOrderId()).isEqualTo(orderId);
        assertThat(captured.getPricePolicyId()).isEqualTo(pricePolicyId);
    }

    @Test
    @DisplayName("주문 상품의 리뷰 상태를 false로 업데이트하면 정상적으로 반영된다")
    void updateReviewStatus_withIsReviewedFalse_updatesSuccessfully() {
        // given
        Long orderId = 2L;
        Long pricePolicyId = 200L;
        OrderProduct orderProduct = buildOrderProduct(orderId, pricePolicyId, true);
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, false);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

        // when
        updateOrderProductService.updateReviewStatus(command);

        // then
        ArgumentCaptor<OrderProduct> captor = ArgumentCaptor.forClass(OrderProduct.class);
        verify(updateOrderProductPort).update(captor.capture());
        assertThat(captor.getValue().getIsReviewed()).isFalse();
    }

    @Test
    @DisplayName("주문 상품의 리뷰 상태를 null로 업데이트하면 null이 반영된다")
    void updateReviewStatus_withIsReviewedNull_updatesWithNull() {
        // given
        Long orderId = 3L;
        Long pricePolicyId = 300L;
        OrderProduct orderProduct = buildOrderProduct(orderId, pricePolicyId, true);
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, null);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

        // when
        updateOrderProductService.updateReviewStatus(command);

        // then
        ArgumentCaptor<OrderProduct> captor = ArgumentCaptor.forClass(OrderProduct.class);
        verify(updateOrderProductPort).update(captor.capture());
        assertThat(captor.getValue().getIsReviewed()).isNull();
    }

    @Test
    @DisplayName("주문 상품이 존재하지 않으면 OrderProductNotFoundException이 발생한다")
    void updateReviewStatus_orderProductNotFound_throwsException() {
        // given
        Long orderId = 999L;
        Long pricePolicyId = 999L;
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, true);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId))
                .thenThrow(new OrderProductNotFoundException(orderId, pricePolicyId));

        // when & then
        assertThatThrownBy(() -> updateOrderProductService.updateReviewStatus(command))
                .isInstanceOf(OrderProductNotFoundException.class);
        verifyNoInteractions(updateOrderProductPort);
    }

    @Test
    @DisplayName("리뷰 상태 업데이트 시 주문 상품 조회 후 업데이트 포트가 순서대로 호출된다")
    void updateReviewStatus_callsGetOrderProductThenUpdatePort_inOrder() {
        // given
        Long orderId = 4L;
        Long pricePolicyId = 400L;
        OrderProduct orderProduct = buildOrderProduct(orderId, pricePolicyId, false);
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, true);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

        // when
        updateOrderProductService.updateReviewStatus(command);

        // then
        InOrder inOrder = inOrder(getOrderUseCase, updateOrderProductPort);
        inOrder.verify(getOrderUseCase).getOrderProduct(orderId, pricePolicyId);
        inOrder.verify(updateOrderProductPort).update(orderProduct);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("리뷰 상태 업데이트 시 UpdateOrderProductPort가 정확히 한 번 호출된다")
    void updateReviewStatus_callsUpdatePortExactlyOnce() {
        // given
        Long orderId = 5L;
        Long pricePolicyId = 500L;
        OrderProduct orderProduct = buildOrderProduct(orderId, pricePolicyId, false);
        UpdateOrderProductReviewStatusCommand command =
                UpdateOrderProductReviewStatusCommand.of(orderId, pricePolicyId, true);
        when(getOrderUseCase.getOrderProduct(orderId, pricePolicyId)).thenReturn(orderProduct);

        // when
        updateOrderProductService.updateReviewStatus(command);

        // then
        verify(getOrderUseCase, times(1)).getOrderProduct(orderId, pricePolicyId);
        verify(updateOrderProductPort, times(1)).update(orderProduct);
        verifyNoMoreInteractions(getOrderUseCase, updateOrderProductPort);
    }

    // --- Helper Methods ---

    private OrderProduct buildOrderProduct(Long orderId, Long pricePolicyId, Boolean isReviewed) {
        return OrderProduct.from(
                OrderProductSnapshotState.builder()
                        .orderId(orderId)
                        .sellerId(10L)
                        .pricePolicyId(pricePolicyId)
                        .quantity(1)
                        .unitAmount(10000L)
                        .orderStatus(OrderStatus.DELIVERED)
                        .isReviewed(isReviewed)
                        .build()
        );
    }
}
