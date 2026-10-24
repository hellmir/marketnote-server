package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentEventNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReserveInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishPaymentEventPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.SavePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePaymentPort;
import com.personal.marketnote.commerce.port.out.payment.UpdatePspPaymentEventPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.PaymentApprovalVendorResult;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.service.saga.OrderPaymentSagaStarter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApprovalTransactionHelper 테스트")
class PaymentApprovalTransactionHelperTest {

    @InjectMocks
    private PaymentApprovalTransactionHelper txHelper;

    @Mock
    private FindOrderPort findOrderPort;

    @Mock
    private FindPaymentPort findPaymentPort;

    @Mock
    private UpdatePaymentPort updatePaymentPort;

    @Mock
    private FindPspPaymentEventPort findPspPaymentEventPort;

    @Mock
    private SavePspPaymentEventPort savePspPaymentEventPort;

    @Mock
    private UpdatePspPaymentEventPort updatePspPaymentEventPort;

    @Mock
    private ChangeOrderStatusUseCase changeOrderStatusUseCase;

    @Mock
    private ReserveInventoryUseCase reserveInventoryUseCase;

    @Mock
    private PublishPaymentEventPort publishPaymentEventPort;

    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;

    @Mock
    private Optional<OrderPaymentSagaStarter> orderPaymentSagaStarter;

    private static final Long BUYER_ID = 100L;
    private static final Long ORDER_ID = 1L;
    private static final UUID ORDER_KEY = UUID.randomUUID();
    private static final String ORDER_KEY_STR = ORDER_KEY.toString();

    @Nested
    @DisplayName("prepareExecution: PAYMENT_EXECUTING 전이")
    class PrepareExecutionTest {

        @Test
        @DisplayName("prepareExecution 시 Order를 PAYMENT_EXECUTING으로 변경하는 changeOrderStatus가 호출된다")
        void shouldChangeOrderStatusToPaymentExecutingOnPrepareExecution() {
            ApprovePaymentCommand command = createApproveCommand();
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent event = createReadyEvent();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            txHelper.prepareExecution(command);

            ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand captured = captor.getValue();
            assertThat(captured.id()).isEqualTo(ORDER_ID);
            assertThat(captured.orderStatus()).isEqualTo(OrderStatus.PAYMENT_EXECUTING);
            assertThat(captured.skipSubsequentProcesses()).isTrue();
        }

        @Test
        @DisplayName("prepareExecution 시 PspPaymentEvent 업데이트와 inventory 예약이 수행된다")
        void shouldUpdatePspPaymentEventAndReserveInventory() {
            ApprovePaymentCommand command = createApproveCommand();
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent event = createReadyEvent();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(event));

            txHelper.prepareExecution(command);

            verify(updatePspPaymentEventPort).update(event);
            verify(reserveInventoryUseCase).reserveInventory(any());
        }

        @Test
        @DisplayName("Payment를 찾을 수 없으면 PaymentNotFoundException이 발생하고 Order 상태는 변경되지 않는다")
        void shouldNotChangeOrderStatusWhenPaymentNotFound() {
            ApprovePaymentCommand command = createApproveCommand();
            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> txHelper.prepareExecution(command))
                    .isInstanceOf(PaymentNotFoundException.class);

            verify(changeOrderStatusUseCase, never()).changeOrderStatus(any());
        }

        @Test
        @DisplayName("PspPaymentEvent를 찾을 수 없으면 PaymentEventNotFoundException이 발생하고 Order 상태는 변경되지 않는다")
        void shouldNotChangeOrderStatusWhenPaymentEventNotFound() {
            ApprovePaymentCommand command = createApproveCommand();
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING);

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> txHelper.prepareExecution(command))
                    .isInstanceOf(PaymentEventNotFoundException.class);

            verify(changeOrderStatusUseCase, never()).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("prepareExecutionForQuickPayment: PAYMENT_EXECUTING 전이")
    class PrepareExecutionForQuickPaymentTest {

        @Test
        @DisplayName("prepareExecutionForQuickPayment 시 Order를 PAYMENT_EXECUTING으로 변경하는 changeOrderStatus가 호출된다")
        void shouldChangeOrderStatusToPaymentExecutingOnQuickPayment() {
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent savedEvent = createReadyEvent();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.empty());
            when(savePspPaymentEventPort.save(any())).thenReturn(savedEvent);

            txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000");

            ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand captured = captor.getValue();
            assertThat(captured.id()).isEqualTo(ORDER_ID);
            assertThat(captured.orderStatus()).isEqualTo(OrderStatus.PAYMENT_EXECUTING);
            assertThat(captured.skipSubsequentProcesses()).isTrue();
        }

        @Test
        @DisplayName("Payment를 찾을 수 없으면 Order 상태가 변경되지 않는다")
        void shouldNotChangeOrderStatusWhenPaymentNotFoundOnQuickPayment() {
            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () -> txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000")
            ).isInstanceOf(PaymentNotFoundException.class);

            verify(changeOrderStatusUseCase, never()).changeOrderStatus(any());
        }
    }

    @Nested
    @DisplayName("commitSuccess: PAID 전이")
    class CommitSuccessTest {

        @Test
        @DisplayName("commitSuccess는 Order를 PAID로 변경하는 changeOrderStatus를 호출한다")
        void shouldChangeOrderStatusToPaidOnCommitSuccess() {
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_EXECUTING);
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPaymentSagaStarter.isPresent()).thenReturn(false);

            txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0));

            ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand captured = captor.getValue();
            assertThat(captured.id()).isEqualTo(ORDER_ID);
            assertThat(captured.orderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("Order 조회 실패 시 OrderNotFoundException이 발생한다")
        void shouldThrowWhenOrderNotFoundOnCommitSuccess() {
            Payment payment = createPayment();
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0)))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("commitFailure: FAILED 전이")
    class CommitFailureTest {

        @Test
        @DisplayName("commitFailure는 Order를 FAILED로 변경하는 changeOrderStatus를 호출한다")
        void shouldChangeOrderStatusToFailedOnCommitFailure() {
            Payment payment = createPayment();
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);

            txHelper.commitFailure(context, "8001", "카드 인증 실패");

            ArgumentCaptor<ChangeOrderStatusCommand> captor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(captor.capture());

            ChangeOrderStatusCommand captured = captor.getValue();
            assertThat(captured.id()).isEqualTo(ORDER_ID);
            assertThat(captured.orderStatus()).isEqualTo(OrderStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("commitUnknown: Order 상태 변경 없음")
    class CommitUnknownTest {

        @Test
        @DisplayName("commitUnknown은 Order 상태를 변경하지 않는다")
        void shouldNotChangeOrderStatusOnCommitUnknown() {
            Payment payment = createPayment();
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);

            txHelper.commitUnknown(context, "UNKNOWN", "Connection timeout");

            verify(changeOrderStatusUseCase, never()).changeOrderStatus(any());
        }
    }

    private ApprovePaymentCommand createApproveCommand() {
        return ApprovePaymentCommand.builder()
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY_STR)
                .encData("enc_data_test")
                .encInfo("enc_info_test")
                .payType("PACA")
                .build();
    }

    private Payment createPayment() {
        PaymentCreateState state = PaymentCreateState.builder()
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY)
                .paymentAmount(50000L)
                .build();
        return Payment.from(state);
    }

    private Order createOrder(OrderStatus status) {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(ORDER_ID)
                        .sellerId(10L)
                        .pricePolicyId(1L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .accumulatedPoint(0L)
                        .orderStatus(status)
                        .build()
        );

        return Order.from(OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderNumber(OrderNumber.of("ORD-TEST-001"))
                .orderStatus(status)
                .amount(OrderAmount.from(OrderAmountCreateState.builder().totalAmount(50000L).paidAmount(null).couponAmount(0L).pointAmount(0L).shippingFee(null).build()))
                .shippingAddress(ShippingAddress.from(ShippingAddressCreateState.builder().recipientName("수령인").recipientPhoneNumber("010-1234-5678").zipCode("12345").address("서울시 강남구").addressDetail("상세주소").deliveryRequestType(null).deliveryRequestMessage(null).build()))
                .orderProductStates(productStates)
                .build());
    }

    private PspPaymentEvent createReadyEvent() {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY_STR)
                .pgCompanyKey("NHN_KCP")
                .pgShopKey("T0000")
                .poStatus(PaymentEventStatus.READY)
                .method("PACA")
                .amount(50000L)
                .build();
        return PspPaymentEvent.from(state);
    }

    private PspPaymentEvent createExecutingEvent() {
        PspPaymentEventSnapshotState state = PspPaymentEventSnapshotState.builder()
                .id(1L)
                .orderId(ORDER_ID)
                .orderKey(ORDER_KEY_STR)
                .pgCompanyKey("NHN_KCP")
                .pgShopKey("T0000")
                .poStatus(PaymentEventStatus.EXECUTING)
                .method("PACA")
                .amount(50000L)
                .build();
        return PspPaymentEvent.from(state);
    }

    private PaymentApprovalVendorResult createSuccessVendorResult() {
        return PaymentApprovalVendorResult.builder()
                .success(true)
                .resultCode("0000")
                .resultMessage("승인 성공")
                .transactionId("tno_123")
                .amount("50000")
                .payMethod("PACA")
                .cardCode("CCLG")
                .cardName("신한카드")
                .cardNumber("1234-****-****-5678")
                .approvalNumber("12345678")
                .approvalTime("20260210153000")
                .installmentMonths("00")
                .partialCancelYn("Y")
                .rawResponse("{\"res_cd\":\"0000\"}")
                .build();
    }
}
