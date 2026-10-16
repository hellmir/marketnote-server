package com.personal.marketnote.product.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.adapter.in.event.saga.payload.CartSagaPayload;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.cart.DeleteCartProductUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedCartSagaConsumer {

    private final ObjectMapper objectMapper;
    private final DeleteCartProductUseCase deleteCartProductUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_CART,
            groupId = "saga-order-payment-downstream-cart"
    )
    public void handleCartStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 결제 완료 장바구니 삭제 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            log.warn("장바구니 삭제 스텝은 보상이 없습니다. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );
        } catch (Exception e) {
            log.error("SAGA 결제 완료 장바구니 삭제 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            CartSagaPayload payload = objectMapper.readValue(stepMessage.payload(), CartSagaPayload.class);

            if (FormatValidator.hasNoValue(payload.buyerId())) {
                publishValidationFailure(stepMessage, "buyerId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.pricePolicyIds()) || payload.pricePolicyIds().isEmpty()) {
                publishValidationFailure(stepMessage, "pricePolicyIds 누락");
                return;
            }

            deleteCartProductUseCase.deleteCartProducts(
                    DeleteCartProductCommand.of(payload.buyerId(), payload.pricePolicyIds())
            );

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 장바구니 삭제 성공. sagaId={}, orderId={}, buyerId={}",
                    stepMessage.sagaId(), payload.orderId(), payload.buyerId()
            );
        } catch (Exception e) {
            log.error("SAGA 장바구니 삭제 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "장바구니 삭제 처리 실패"
            );
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 장바구니 삭제 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason
        );
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
