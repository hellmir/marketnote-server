package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.personal.marketnote.commerce.adapter.in.event.saga.payload.PaymentCompletedSagaPayload;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.service.saga.OrderPaymentCompletedSagaStarter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentCompletedSagaActionDispatcher 테스트")
class OrderPaymentCompletedSagaActionDispatcherTest {

    @InjectMocks
    private OrderPaymentCompletedSagaActionDispatcher dispatcher;

    @Mock
    private PublishOrderEventPort publishOrderEventPort;

    @Mock
    private OrderPaymentCompletedSagaStarter orderPaymentCompletedSagaStarter;

    private PaymentCompletedSagaPayload buildPayload(String recipientName, String address) {
        return new PaymentCompletedSagaPayload(
                1L, 50L, 80000L, 1000L, 500L,
                recipientName, "010-1234-5678", "12345", address, "상세주소",
                "문 앞에 놓아주세요",
                Collections.emptyList()
        );
    }

    private OrderPaymentCompletedSagaContext buildDownstreamContext() {
        return OrderPaymentCompletedSagaContext.builder()
                .orderId(1L)
                .buyerId(50L)
                .totalAmount(80000L)
                .pointAmount(1000L)
                .totalAccumulatedPoint(500L)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .zipCode("12345")
                .address("서울시 강남구")
                .addressDetail("상세주소")
                .deliveryRequestMessage("문 앞에 놓아주세요")
                .orderProducts(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("정상 Payload 수신 시 ORDER_PAYMENT_COMPLETED 이벤트를 발행하고 다운스트림 SAGA를 시작한다")
    void dispatch_validPayload_publishesEventAndStartsSaga() {
        // given
        PaymentCompletedSagaPayload payload = buildPayload("홍길동", "서울시 강남구");
        OrderPaymentCompletedSagaContext downstreamContext = buildDownstreamContext();
        List<OrderProduct> orderProducts = Collections.emptyList();

        // when
        dispatcher.dispatch(payload, orderProducts, downstreamContext);

        // then
        ArgumentCaptor<ShippingAddress> shippingAddressCaptor = ArgumentCaptor.forClass(ShippingAddress.class);
        verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                eq(1L), eq(50L), eq(80000L), eq(1000L),
                shippingAddressCaptor.capture(), eq(orderProducts), eq(500L)
        );

        ShippingAddress capturedAddress = shippingAddressCaptor.getValue();
        assertThat(capturedAddress).isNotNull();
        assertThat(capturedAddress.getZipCode()).isEqualTo("12345");
        assertThat(capturedAddress.getAddress()).isEqualTo("서울시 강남구");
        assertThat(capturedAddress.getAddressDetail()).isEqualTo("상세주소");

        verify(orderPaymentCompletedSagaStarter).start(downstreamContext);
    }

    @Test
    @DisplayName("recipientName과 address가 모두 null이면 ShippingAddress를 NULL로 전달한다")
    void dispatch_missingRecipientAndAddress_passesNullShippingAddress() {
        // given
        PaymentCompletedSagaPayload payload = buildPayload(null, null);
        OrderPaymentCompletedSagaContext downstreamContext = buildDownstreamContext();
        List<OrderProduct> orderProducts = Collections.emptyList();

        // when
        dispatcher.dispatch(payload, orderProducts, downstreamContext);

        // then
        verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                eq(1L), eq(50L), eq(80000L), eq(1000L),
                isNull(), eq(orderProducts), eq(500L)
        );
        verify(orderPaymentCompletedSagaStarter).start(downstreamContext);
    }

    @Test
    @DisplayName("recipientName만 있고 address가 null이면 ShippingAddress는 NULL이 아닌 값으로 생성된다")
    void dispatch_onlyRecipientName_buildsShippingAddress() {
        // given
        PaymentCompletedSagaPayload payload = buildPayload("홍길동", null);
        OrderPaymentCompletedSagaContext downstreamContext = buildDownstreamContext();

        // when
        dispatcher.dispatch(payload, Collections.emptyList(), downstreamContext);

        // then
        ArgumentCaptor<ShippingAddress> shippingAddressCaptor = ArgumentCaptor.forClass(ShippingAddress.class);
        verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                any(), any(), any(), any(),
                shippingAddressCaptor.capture(), any(), any()
        );

        ShippingAddress captured = shippingAddressCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getAddress()).isNull();
    }

    @Test
    @DisplayName("address만 있고 recipientName이 null이면 ShippingAddress는 NULL이 아닌 값으로 생성된다")
    void dispatch_onlyAddress_buildsShippingAddress() {
        // given
        PaymentCompletedSagaPayload payload = buildPayload(null, "서울시 강남구");
        OrderPaymentCompletedSagaContext downstreamContext = buildDownstreamContext();

        // when
        dispatcher.dispatch(payload, Collections.emptyList(), downstreamContext);

        // then
        ArgumentCaptor<ShippingAddress> shippingAddressCaptor = ArgumentCaptor.forClass(ShippingAddress.class);
        verify(publishOrderEventPort).publishOrderPaymentCompletedEvent(
                any(), any(), any(), any(),
                shippingAddressCaptor.capture(), any(), any()
        );

        ShippingAddress captured = shippingAddressCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getAddress()).isEqualTo("서울시 강남구");
        assertThat(captured.getRecipientName()).isNull();
    }

    @Test
    @DisplayName("dispatch 메서드는 @Transactional(READ_COMMITTED)로 선언되어 있다")
    void dispatch_method_declaresTransactionalReadCommitted() throws NoSuchMethodException {
        // given
        Method method = OrderPaymentCompletedSagaActionDispatcher.class.getDeclaredMethod(
                "dispatch",
                PaymentCompletedSagaPayload.class, List.class, OrderPaymentCompletedSagaContext.class
        );

        // when
        Transactional annotation = method.getAnnotation(Transactional.class);

        // then
        assertThat(annotation).isNotNull();
        assertThat(annotation.isolation()).isEqualTo(Isolation.READ_COMMITTED);
    }
}
