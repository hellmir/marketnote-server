package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.domain.payment.*;
import com.personal.marketnote.commerce.exception.DuplicatePaymentReadyException;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentEventNotFoundException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.inventory.ReserveInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext;
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
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.service.saga.OrderPaymentSagaStarter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

            ArgumentCaptor<ReserveInventoryCommand> reserveCaptor = ArgumentCaptor.forClass(ReserveInventoryCommand.class);
            verify(reserveInventoryUseCase).reserveInventory(reserveCaptor.capture());
            ReserveInventoryCommand reserveCommand = reserveCaptor.getValue();
            assertThat(reserveCommand.orderId()).isEqualTo(ORDER_ID);
            assertThat(reserveCommand.orderProducts()).hasSize(1);
            assertThat(reserveCommand.orderProducts().get(0).pricePolicyId()).isEqualTo(1L);
            assertThat(reserveCommand.orderProducts().get(0).quantity()).isEqualTo(1);
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

        @Test
        @DisplayName("빠른결제 경로에서 미해결 PSP 이벤트가 존재하면 DuplicatePaymentReadyException이 발생하고 save/changeOrderStatus가 호출되지 않는다")
        void shouldThrowDuplicatePaymentReadyWhenUnresolvedPspEventExists() {
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_PENDING);
            PspPaymentEvent unresolvedEvent = createExecutingEvent();

            when(findPaymentPort.findByOrderKey(ORDER_KEY)).thenReturn(Optional.of(payment));
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(findPspPaymentEventPort.findByOrderKey(ORDER_KEY_STR)).thenReturn(Optional.of(unresolvedEvent));

            assertThatThrownBy(
                    () -> txHelper.prepareExecutionForQuickPayment(BUYER_ID, ORDER_KEY_STR, "NHN_KCP", "T0000")
            ).isInstanceOf(DuplicatePaymentReadyException.class);

            verify(savePspPaymentEventPort, never()).save(any());
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

        @Test
        @DisplayName("Saga 활성 분기에서는 skipSubsequentProcesses=true로 PAID 전이 후 Saga start를 호출하고 결제 승인 이벤트를 발행하지 않는다")
        void shouldStartSagaAndSkipPublishWhenSagaStarterPresent() {
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_EXECUTING);
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            OrderPaymentSagaStarter sagaStarter = org.mockito.Mockito.mock(OrderPaymentSagaStarter.class);
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPaymentSagaStarter.isPresent()).thenReturn(true);
            when(orderPaymentSagaStarter.get()).thenReturn(sagaStarter);

            txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0));

            ArgumentCaptor<ChangeOrderStatusCommand> statusCaptor = ArgumentCaptor.forClass(ChangeOrderStatusCommand.class);
            verify(changeOrderStatusUseCase).changeOrderStatus(statusCaptor.capture());
            ChangeOrderStatusCommand captured = statusCaptor.getValue();
            assertThat(captured.orderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(captured.skipSubsequentProcesses()).isTrue();

            verify(sagaStarter).start(any(OrderPaymentSagaContext.class));
            verify(publishPaymentEventPort, never()).publishPaymentApprovedEvent(any(), any(), any());
        }

        @Test
        @DisplayName("Saga 비활성 분기에서는 결제 승인 이벤트만 발행되고 Saga start는 호출되지 않는다")
        void shouldPublishPaymentApprovedEventWhenSagaStarterAbsent() {
            Payment payment = createPayment();
            Order order = createOrder(OrderStatus.PAYMENT_EXECUTING);
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPaymentSagaStarter.isPresent()).thenReturn(false);

            txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0));

            verify(publishPaymentEventPort).publishPaymentApprovedEvent(
                    ORDER_ID, ORDER_KEY_STR, payment.getPaymentAmount().getValue()
            );
            verify(orderPaymentSagaStarter, never()).get();
        }

        @Test
        @DisplayName("OrderProduct 중 일부만 적립 스냅샷을 보유한 경우 DB에서 조회한 상품 정보로 totalAccumulatedPoint를 산출하여 Saga Context에 반영한다")
        void shouldFallbackToProductDbWhenSnapshotPartiallyMissing() throws Exception {
            Payment payment = createPayment();
            Order order = createOrderWithTwoProducts();
            clearAccumulatedPoint(order.getOrderProducts().get(0));

            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            OrderPaymentSagaStarter sagaStarter = org.mockito.Mockito.mock(OrderPaymentSagaStarter.class);
            Map<Long, ProductInfoResult> productInfoMap = new HashMap<>();
            productInfoMap.put(11L, new ProductInfoResult(
                    101L, UUID.randomUUID(), 10L, "상품A", "브랜드A",
                    10000L, null, 500L, List.of()
            ));
            productInfoMap.put(22L, new ProductInfoResult(
                    102L, UUID.randomUUID(), 10L, "상품B", "브랜드B",
                    20000L, null, 1000L, List.of()
            ));

            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPaymentSagaStarter.isPresent()).thenReturn(true);
            when(orderPaymentSagaStarter.get()).thenReturn(sagaStarter);
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList())).thenReturn(productInfoMap);

            txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0));

            verify(findProductByPricePolicyPort).findByPricePolicyIds(List.of(11L, 22L));

            ArgumentCaptor<OrderPaymentSagaContext> sagaCaptor = ArgumentCaptor.forClass(OrderPaymentSagaContext.class);
            verify(sagaStarter).start(sagaCaptor.capture());
            OrderPaymentSagaContext sagaContext = sagaCaptor.getValue();
            // 상품A(500 * 1) + 상품B(1000 * 2) = 2500
            assertThat(sagaContext.totalAccumulatedPoint()).isEqualTo(2500L);
            assertThat(sagaContext.orderProducts()).hasSize(2);
        }

        @Test
        @DisplayName("ShippingAddress가 null인 Order는 Saga Context의 수령인/연락처/주소/배송메시지가 모두 null로 설정된다")
        void shouldSetNullShippingSnapshotWhenShippingAddressIsNull() {
            Payment payment = createPayment();
            Order order = createOrderWithoutShippingAddress();
            PspPaymentEvent event = createExecutingEvent();
            PaymentApprovalContext context = PaymentApprovalContext.of(payment, event);
            PaymentApprovalVendorResult vendorResult = createSuccessVendorResult();

            OrderPaymentSagaStarter sagaStarter = org.mockito.Mockito.mock(OrderPaymentSagaStarter.class);
            when(findOrderPort.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderPaymentSagaStarter.isPresent()).thenReturn(true);
            when(orderPaymentSagaStarter.get()).thenReturn(sagaStarter);

            txHelper.commitSuccess(context, vendorResult, Installment.of((short) 0));

            ArgumentCaptor<OrderPaymentSagaContext> sagaCaptor = ArgumentCaptor.forClass(OrderPaymentSagaContext.class);
            verify(sagaStarter).start(sagaCaptor.capture());
            OrderPaymentSagaContext sagaContext = sagaCaptor.getValue();

            assertThat(sagaContext.recipientName()).isNull();
            assertThat(sagaContext.recipientPhoneNumber()).isNull();
            assertThat(sagaContext.zipCode()).isNull();
            assertThat(sagaContext.address()).isNull();
            assertThat(sagaContext.addressDetail()).isNull();
            assertThat(sagaContext.deliveryRequestMessage()).isNull();
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

    @Nested
    @DisplayName("@Transactional 애노테이션: REQUIRES_NEW + READ_COMMITTED")
    class TransactionAnnotationTest {

        @ParameterizedTest(name = "{0} 메서드는 @Transactional(REQUIRES_NEW, READ_COMMITTED)로 선언되어 있다")
        @ValueSource(strings = {
                "prepareExecution",
                "prepareExecutionForQuickPayment",
                "commitSuccess",
                "commitFailure",
                "commitUnknown"
        })
        void shouldDeclareRequiresNewAndReadCommitted(String methodName) {
            Method method = findPublicMethod(methodName);
            Transactional annotation = method.getAnnotation(Transactional.class);
            assertThat(annotation).isNotNull();
            assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
            assertThat(annotation.isolation()).isEqualTo(Isolation.READ_COMMITTED);
        }

        private Method findPublicMethod(String methodName) {
            for (Method method : PaymentApprovalTransactionHelper.class.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            throw new AssertionError("메서드를 찾을 수 없습니다: " + methodName);
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
        return createOrder(status, productStates, createDefaultShippingAddress());
    }

    private Order createOrderWithTwoProducts() {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(ORDER_ID)
                        .productId(101L)
                        .productKey(UUID.randomUUID())
                        .sellerId(10L)
                        .pricePolicyId(11L)
                        .orderProductKey(UUID.randomUUID())
                        .quantity(1)
                        .unitAmount(10000L)
                        .accumulatedPoint(100L)
                        .orderStatus(OrderStatus.PAYMENT_EXECUTING)
                        .build(),
                OrderProductSnapshotState.builder()
                        .orderId(ORDER_ID)
                        .productId(102L)
                        .productKey(UUID.randomUUID())
                        .sellerId(10L)
                        .pricePolicyId(22L)
                        .orderProductKey(UUID.randomUUID())
                        .quantity(2)
                        .unitAmount(20000L)
                        .accumulatedPoint(100L)
                        .orderStatus(OrderStatus.PAYMENT_EXECUTING)
                        .build()
        );
        return createOrder(OrderStatus.PAYMENT_EXECUTING, productStates, createDefaultShippingAddress());
    }

    private Order createOrderWithoutShippingAddress() {
        List<OrderProductSnapshotState> productStates = List.of(
                OrderProductSnapshotState.builder()
                        .orderId(ORDER_ID)
                        .sellerId(10L)
                        .pricePolicyId(1L)
                        .quantity(1)
                        .unitAmount(50000L)
                        .accumulatedPoint(0L)
                        .orderStatus(OrderStatus.PAYMENT_EXECUTING)
                        .build()
        );
        return createOrder(OrderStatus.PAYMENT_EXECUTING, productStates, null);
    }

    private Order createOrder(
            OrderStatus status, List<OrderProductSnapshotState> productStates, ShippingAddress shippingAddress
    ) {
        return Order.from(OrderSnapshotState.builder()
                .id(ORDER_ID)
                .buyerId(BUYER_ID)
                .orderKey(ORDER_KEY)
                .orderNumber(OrderNumber.of("ORD-TEST-001"))
                .orderStatus(status)
                .amount(createDefaultOrderAmount())
                .shippingAddress(shippingAddress)
                .orderProductStates(productStates)
                .build()
        );
    }

    private OrderAmount createDefaultOrderAmount() {
        return OrderAmount.from(OrderAmountCreateState.builder()
                .totalAmount(50000L)
                .paidAmount(null)
                .couponAmount(0L)
                .pointAmount(0L)
                .shippingFee(null)
                .build()
        );
    }

    private ShippingAddress createDefaultShippingAddress() {
        return ShippingAddress.from(ShippingAddressCreateState.builder()
                .recipientName("수령인")
                .recipientPhoneNumber("010-1234-5678")
                .zipCode("12345")
                .address("서울시 강남구")
                .addressDetail("상세주소")
                .deliveryRequestType(null)
                .deliveryRequestMessage(null)
                .build()
        );
    }

    // Money VO가 null을 거부하므로 스냅샷 부분 누락 상황은 reflection 외 재현 수단이 없다
    private void clearAccumulatedPoint(OrderProduct op) throws ReflectiveOperationException {
        Field f = OrderProduct.class.getDeclaredField("accumulatedPoint");
        f.setAccessible(true);
        f.set(op, null);
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
