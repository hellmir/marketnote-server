package com.personal.marketnote.commerce.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.in.event.saga.payload.CompleteCancellationSagaPayload;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand.CancelProductItemCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CompleteCancelOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import java.util.List;
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
public class OrderCancelCompleteSagaConsumer {

    private final ObjectMapper objectMapper;
    private final CompleteCancelOrderUseCase completeCancelOrderUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;
    private final PublishOrderEventPort publishOrderEventPort;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_CANCEL_COMPLETED,
            groupId = "saga-order-cancel-completed"
    )
    public void handleCompleteCancellationStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

        log.info("SAGA 주문 취소 완료 스텝 수신. sagaId={}, messageType={}", stepMessage.sagaId(), stepMessage.messageType());

        if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
            handleAction(stepMessage);
            acknowledgment.acknowledge();
            return;
        }

        log.warn("주문 취소 완료 스텝은 보상이 없음. 알 수 없는 messageType. sagaId={}, messageType={}",
                stepMessage.sagaId(), stepMessage.messageType()
        );
        acknowledgment.acknowledge();
    }

    private void handleAction(SagaStepMessage stepMessage) {
        CompleteCancellationSagaPayload payload = parsePayload(stepMessage);
        if (FormatValidator.hasNoValue(payload)) {
            return;
        }

        if (FormatValidator.hasNoValue(payload.orderId())) {
            publishValidationFailure(stepMessage, payload, "orderId 누락");
            return;
        }

        if (FormatValidator.hasNoValue(payload.orderKey())) {
            publishValidationFailure(stepMessage, payload, "orderKey 누락");
            return;
        }

        try {
            CompleteCancelOrderCommand command = CompleteCancelOrderCommand.builder()
                    .orderId(payload.orderId())
                    .orderKey(payload.orderKey())
                    .buyerId(payload.buyerId())
                    .cancelAmount(payload.cancelAmount())
                    .paymentAmount(payload.paymentAmount())
                    .pointAmount(payload.pointAmount())
                    .shippingFee(payload.shippingFee())
                    .isFullCancel(payload.isFullCancel())
                    .alreadyRefunded(payload.alreadyRefunded())
                    .reasonCategory(payload.reasonCategory())
                    .reason(payload.reason())
                    .cancelProducts(mapToCancelProductItemCommands(payload.cancelProducts()))
                    .cancelId(payload.cancelId())
                    .build();

            completeCancelOrderUseCase.completeCancellation(command);

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 주문 취소 완료 처리 성공. sagaId={}, orderId={}, orderKey={}",
                    stepMessage.sagaId(), payload.orderId(), payload.orderKey()
            );
        } catch (Exception e) {
            log.error("SAGA 주문 취소 완료 처리 실패. sagaId={}, error={}", stepMessage.sagaId(), e.getMessage(), e);
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "주문 취소 완료 처리 실패"
            );
            publishOrderCancelFailedSafely(payload);
        }
    }

    private CompleteCancellationSagaPayload parsePayload(SagaStepMessage stepMessage) {
        try {
            return objectMapper.readValue(stepMessage.payload(), CompleteCancellationSagaPayload.class);
        } catch (Exception e) {
            log.error("SAGA 주문 취소 완료 스텝 페이로드 역직렬화 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "페이로드 역직렬화 실패"
            );
            return null;
        }
    }

    private void publishOrderCancelFailedSafely(CompleteCancellationSagaPayload payload) {
        if (FormatValidator.hasNoValue(payload)
                || FormatValidator.hasNoValue(payload.orderId())
                || FormatValidator.hasNoValue(payload.buyerId())) {
            return;
        }
        try {
            publishOrderEventPort.publishOrderCancelFailedEvent(payload.orderId(), payload.buyerId());
        } catch (Exception e) {
            log.error("OrderCancelFailedEvent 발행 실패. orderId={}, buyerId={}",
                    payload.orderId(), payload.buyerId(), e
            );
        }
    }

    private List<CancelProductItemCommand> mapToCancelProductItemCommands(
            List<CompleteCancellationSagaPayload.OrderProductItem> items
    ) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(item -> new CancelProductItemCommand(
                        item.pricePolicyId(), item.sharerKey(), item.quantity(), item.unitAmount()
                ))
                .toList();
    }

    private void publishValidationFailure(
            SagaStepMessage stepMessage, CompleteCancellationSagaPayload payload, String reason
    ) {
        log.warn("SAGA 주문 취소 완료 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
        publishOrderCancelFailedSafely(payload);
    }
}
