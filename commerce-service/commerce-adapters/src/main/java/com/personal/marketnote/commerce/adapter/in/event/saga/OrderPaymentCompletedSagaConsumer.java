package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.PaymentCompletedSagaPayload;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.SagaOrderProductItem;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentCompletedSagaContext;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedSagaConsumer {

    private final ObjectMapper objectMapper;
    private final SagaResponsePublisher sagaResponsePublisher;
    private final OrderPaymentCompletedSagaActionDispatcher actionDispatcher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_COMPLETED,
            groupId = "saga-order-payment-completed"
    )
    public void handlePaymentCompletedStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

        log.info("SAGA 결제 완료 이벤트 발행 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

        if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
            handleAction(stepMessage);
            acknowledgment.acknowledge();
            return;
        }

        log.warn("결제 완료 스텝은 보상이 없습니다. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());
        acknowledgment.acknowledge();
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            PaymentCompletedSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), PaymentCompletedSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.orderId()) || FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "orderId 또는 buyerId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "orderProducts 누락");
                return;
            }

            List<OrderProduct> orderProducts = SagaOrderProductMapper.toOrderProducts(payload.orderProducts());

            actionDispatcher.dispatch(payload, orderProducts, toDownstreamContext(payload));

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 결제 완료 이벤트 발행 + 다운스트림 SAGA 시작 성공. sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId()
            );
        } catch (Exception e) {
            log.error("SAGA 결제 완료 이벤트 발행 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "결제 완료 이벤트 발행 실패"
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 결제 완료 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }

    private OrderPaymentCompletedSagaContext toDownstreamContext(PaymentCompletedSagaPayload payload) {
        List<OrderPaymentCompletedSagaContext.OrderProductItem> downstreamProducts = payload.orderProducts().stream()
                .map(this::toDownstreamProduct)
                .toList();
        return OrderPaymentCompletedSagaContext.builder()
                .orderId(payload.orderId())
                .buyerId(payload.buyerId())
                .totalAmount(payload.totalAmount())
                .pointAmount(payload.pointAmount())
                .totalAccumulatedPoint(payload.totalAccumulatedPoint())
                .recipientName(payload.recipientName())
                .recipientPhoneNumber(payload.recipientPhoneNumber())
                .zipCode(payload.zipCode())
                .address(payload.address())
                .addressDetail(payload.addressDetail())
                .deliveryRequestMessage(payload.deliveryRequestMessage())
                .orderProducts(downstreamProducts)
                .build();
    }

    private OrderPaymentCompletedSagaContext.OrderProductItem toDownstreamProduct(SagaOrderProductItem item) {
        return new OrderPaymentCompletedSagaContext.OrderProductItem(
                item.pricePolicyId(), item.productId(), item.sharerKey(), item.quantity(), item.unitAmount()
        );
    }
}
