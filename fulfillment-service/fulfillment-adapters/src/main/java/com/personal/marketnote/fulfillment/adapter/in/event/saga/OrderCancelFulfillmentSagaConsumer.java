package com.personal.marketnote.fulfillment.adapter.in.event.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.event.saga.payload.OrderCancelFulfillmentSagaPayload;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishOrderCancelFailedEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 주문 취소 SAGA의 풀필먼트 스텝 Consumer.
 * <p>
 * productKeys 규약:
 * <ul>
 *     <li>비어있지 않은 리스트: 해당 productKey에 대응되는 출고 항목만 취소</li>
 *     <li>null 또는 빈 리스트: 주문 전체 취소로 해석되어 UseCase에서 orderNumber 단위로 취소 요청</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderCancelFulfillmentSagaConsumer {

    private static final String REQUIRES_FULFILLMENT_CANCEL = "PREPARING";

    private final ObjectMapper objectMapper;
    private final CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;
    private final PublishOrderCancelFailedEventPort publishOrderCancelFailedEventPort;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_CANCEL_FULFILLMENT,
            groupId = "saga-order-cancel-fulfillment-service"
    )
    public void handleOrderCancelFulfillmentStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 주문 취소 풀필먼트 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );

            if (!SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                log.warn("주문 취소 풀필먼트 스텝은 보상이 없음. 지원하지 않는 messageType. sagaId={}, messageType={}",
                        stepMessage.sagaId(), stepMessage.messageType()
                );
                sagaResponsePublisher.publishFailure(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(),
                        "지원하지 않는 messageType: " + stepMessage.messageType()
                );
                return;
            }

            handleAction(stepMessage);
        } catch (Exception e) {
            log.error("SAGA 주문 취소 풀필먼트 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        Long orderId = null;
        Long buyerId = null;
        try {
            OrderCancelFulfillmentSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), OrderCancelFulfillmentSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, null, null, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.originalStatus())) {
                publishValidationFailure(stepMessage, payload.orderId(), payload.buyerId(), "originalStatus 누락");
                return;
            }

            orderId = payload.orderId();
            buyerId = payload.buyerId();

            if (!REQUIRES_FULFILLMENT_CANCEL.equals(payload.originalStatus())) {
                log.info("풀필먼트 취소 불필요 (originalStatus={}). sagaId={}, orderId={}",
                        payload.originalStatus(), stepMessage.sagaId(), orderId
                );
                sagaResponsePublisher.publishSuccess(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), buildSkippedResponse(payload.originalStatus())
                );
                return;
            }

            List<String> productKeys = FormatValidator.hasValue(payload.productKeys())
                    ? payload.productKeys()
                    : List.of();

            CancelInternalFulfillmentDeliveryResult result = cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(
                    new CancelInternalFulfillmentDeliveryCommand(orderId, productKeys)
            );

            if (!result.cancelled()) {
                log.warn("주문 취소 풀필먼트 처리 거부. sagaId={}, orderId={}, message={}",
                        stepMessage.sagaId(), orderId, result.message()
                );
                sagaResponsePublisher.publishFailure(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(),
                        "주문 취소 풀필먼트 처리 거부: orderId=" + orderId + ", " + result.message()
                );
                publishOrderCancelFailedSafely(orderId, buyerId);
                return;
            }

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 주문 취소 풀필먼트 처리 성공. sagaId={}, orderId={}",
                    stepMessage.sagaId(), orderId
            );
        } catch (FulfillmentDeliveryRegistrationNotFoundException e) {
            log.info("출고 등록 없음 (멱등 처리). sagaId={}, orderId={}, message={}",
                    stepMessage.sagaId(), orderId, e.getMessage()
            );
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true,\"idempotent\":true}"
            );
        } catch (FulfillmentDeliveryCancellationNotAllowedException e) {
            log.warn("주문 취소 풀필먼트 처리 거부 (출고 진행/완료). sagaId={}, orderId={}, message={}",
                    stepMessage.sagaId(), orderId, e.getMessage()
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(),
                    "주문 취소 풀필먼트 처리 거부: orderId=" + orderId + ", " + e.getMessage()
            );
            publishOrderCancelFailedSafely(orderId, buyerId);
        } catch (Exception e) {
            log.error("SAGA 주문 취소 풀필먼트 처리 실패. sagaId={}, orderId={}, error={}",
                    stepMessage.sagaId(), orderId, e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(),
                    "주문 취소 풀필먼트 처리 실패: orderId=" + orderId
            );
            publishOrderCancelFailedSafely(orderId, buyerId);
        }
    }

    private String buildSkippedResponse(String originalStatus) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("compensated", true);
        body.put("reason", "fulfillment-cancel-not-required");
        body.put("skipped", true);
        body.put("originalStatus", originalStatus);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.warn("skipped 응답 직렬화 실패, 기본 메시지 사용. originalStatus={}, error={}",
                    originalStatus, e.getMessage()
            );
            return "{\"compensated\":true,\"reason\":\"fulfillment-cancel-not-required\",\"skipped\":true}";
        }
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, Long orderId, Long buyerId, String reason) {
        log.warn("SAGA 주문 취소 풀필먼트 스텝 페이로드 검증 실패. sagaId={}, reason={}",
                stepMessage.sagaId(), reason
        );
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
        publishOrderCancelFailedSafely(orderId, buyerId);
    }

    private void publishOrderCancelFailedSafely(Long orderId, Long buyerId) {
        if (FormatValidator.hasNoValue(orderId) || FormatValidator.hasNoValue(buyerId)) {
            return;
        }
        try {
            publishOrderCancelFailedEventPort.publish(orderId, buyerId);
        } catch (Exception e) {
            log.error("OrderCancelFailedEvent 발행 실패. orderId={}, buyerId={}",
                    orderId, buyerId, e
            );
        }
    }
}
