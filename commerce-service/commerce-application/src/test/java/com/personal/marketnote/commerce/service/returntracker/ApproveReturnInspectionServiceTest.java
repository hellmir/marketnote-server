package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.ApproveReturnInspectionCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApproveReturnInspectionService 테스트")
class ApproveReturnInspectionServiceTest {

    @InjectMocks
    private ApproveReturnInspectionService service;

    @Mock
    private GetOrderUseCase getOrderUseCase;

    @Mock
    private CompleteReturnUseCase completeReturnUseCase;

    private static final Long ORDER_ID = 100L;

    @Nested
    @DisplayName("반품 검수 승인 성공")
    class ApproveSuccess {

        @Test
        @DisplayName("RETURN_INSPECTING 상태의 주문에서 CompleteReturnUseCase.completeReturn이 호출된다")
        void shouldDelegateToCompleteReturnUseCaseWhenReturnInspecting() {
            Order order = createReturnInspectingOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.approveReturnInspection(new ApproveReturnInspectionCommand(ORDER_ID));

            verify(completeReturnUseCase).completeReturn(new CompleteReturnCommand(ORDER_ID));
        }
    }

    @Nested
    @DisplayName("멱등성 처리")
    class Idempotency {

        @Test
        @DisplayName("이미 RETURNED 상태이면 아무 처리 없이 정상 종료한다")
        void shouldReturnEarlyWhenAlreadyReturned() {
            Order order = createReturnedOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            service.approveReturnInspection(new ApproveReturnInspectionCommand(ORDER_ID));

            verifyNoInteractions(completeReturnUseCase);
        }
    }

    @Nested
    @DisplayName("상태 전이 실패")
    class TransitionFailure {

        @Test
        @DisplayName("RETURN_INSPECTING이 아닌 상태이면 InvalidOrderStatusTransitionException을 던진다")
        void shouldThrowExceptionWhenNotReturnInspecting() {
            Order order = createNonInspectingOrder();
            when(getOrderUseCase.getOrder(ORDER_ID)).thenReturn(order);

            assertThatThrownBy(() ->
                    service.approveReturnInspection(new ApproveReturnInspectionCommand(ORDER_ID))
            ).isInstanceOf(InvalidOrderStatusTransitionException.class);

            verifyNoInteractions(completeReturnUseCase);
        }
    }

    private Order createReturnInspectingOrder() {
        Order order = mock(Order.class);
        when(order.isReturnInspecting()).thenReturn(true);
        return order;
    }

    private Order createReturnedOrder() {
        Order order = mock(Order.class);
        when(order.isReturned()).thenReturn(true);
        return order;
    }

    private Order createNonInspectingOrder() {
        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.RETURN_IN_PROGRESS);
        return order;
    }
}
