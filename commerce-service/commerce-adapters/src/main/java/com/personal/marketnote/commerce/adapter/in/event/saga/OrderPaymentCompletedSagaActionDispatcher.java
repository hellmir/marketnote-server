package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.personal.marketnote.commerce.adapter.in.event.saga.payload.PaymentCompletedSagaPayload;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import com.personal.marketnote.commerce.domain.order.ShippingAddressCreateState;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.service.saga.OrderPaymentCompletedSagaStarter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 완료 SAGA 최종 스텝 액션 핸들러.
 *
 * <p>ORDER_PAYMENT_COMPLETED 이벤트 발행과 다운스트림 SAGA 시작을 하나의 트랜잭션으로 묶어
 * outbox 저장 원자성을 보장한다. Consumer 내부에서 호출하는 경우 self-invocation으로
 * 프록시 AOP가 동작하지 않으므로 별도 빈으로 분리했다.</p>
 */
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedSagaActionDispatcher {

    private final PublishOrderEventPort publishOrderEventPort;
    private final OrderPaymentCompletedSagaStarter orderPaymentCompletedSagaStarter;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void dispatch(
            PaymentCompletedSagaPayload payload, List<OrderProduct> orderProducts,
            OrderPaymentCompletedSagaContext downstreamContext
    ) {
        ShippingAddress shippingAddress = buildShippingAddress(payload);

        publishOrderEventPort.publishOrderPaymentCompletedEvent(
                payload.orderId(), payload.buyerId(), payload.totalAmount(),
                payload.pointAmount(), shippingAddress, orderProducts, payload.totalAccumulatedPoint()
        );

        orderPaymentCompletedSagaStarter.start(downstreamContext);
    }

    private ShippingAddress buildShippingAddress(PaymentCompletedSagaPayload payload) {
        if (FormatValidator.hasNoValue(payload.recipientName())
                && FormatValidator.hasNoValue(payload.address())) {
            return null;
        }
        return ShippingAddress.from(ShippingAddressCreateState.builder().recipientName(payload.recipientName()).recipientPhoneNumber(payload.recipientPhoneNumber()).zipCode(payload.zipCode()).address(payload.address()).addressDetail(payload.addressDetail()).deliveryRequestType(null).deliveryRequestMessage(payload.deliveryRequestMessage()).build());
    }
}
