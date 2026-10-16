package com.personal.marketnote.fulfillment.adapter.in.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.saga.SagaResponsePublisher;
import com.personal.marketnote.common.saga.SagaStepMessage;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.adapter.in.event.saga.payload.FulfillmentCancelSagaPayload;
import com.personal.marketnote.fulfillment.adapter.in.event.saga.payload.FulfillmentDeliverySagaPayload;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryCancellationNotAllowedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryRegistrationNotFoundException;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
import com.personal.marketnote.fulfillment.port.in.command.CancelInternalFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.CancelInternalFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.CancelInternalFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class OrderPaymentCompletedFulfillmentSagaConsumer {

    private final ObjectMapper objectMapper;
    private final CreateShippingTrackerUseCase createShippingTrackerUseCase;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;
    private final CancelInternalFulfillmentDeliveryUseCase cancelInternalFulfillmentDeliveryUseCase;
    private final SagaResponsePublisher sagaResponsePublisher;
    private final FulfillmentAuthProperties fasstoAuthProperties;
    private final Clock clock;

    @KafkaListener(
            topics = KafkaTopicConstants.SAGA_ORDER_PAYMENT_DOWNSTREAM_FULFILLMENT,
            groupId = "saga-order-payment-downstream-fulfillment"
    )
    public void handleFulfillmentStep(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        try {
            EventEnvelope<?> envelope = record.value();

            if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
                return;
            }

            SagaStepMessage stepMessage = envelope.getPayloadAs(SagaStepMessage.class, objectMapper);

            log.info("SAGA 결제 완료 풀필먼트 출고 스텝 수신. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );

            if (SagaStepMessage.ACTION.equals(stepMessage.messageType())) {
                handleAction(stepMessage);
                return;
            }

            if (SagaStepMessage.COMPENSATION.equals(stepMessage.messageType())) {
                handleCompensation(stepMessage);
                return;
            }

            log.warn("알 수 없는 messageType. sagaId={}, messageType={}",
                    stepMessage.sagaId(), stepMessage.messageType()
            );
        } catch (Exception e) {
            log.error("SAGA 결제 완료 풀필먼트 출고 스텝 처리 실패. topic={}, partition={}, offset={}, error={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e
            );
        } finally {
            acknowledgment.acknowledge();
        }
    }

    private void handleAction(SagaStepMessage stepMessage) {
        try {
            FulfillmentDeliverySagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), FulfillmentDeliverySagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }
            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                publishValidationFailure(stepMessage, "orderProducts 누락");
                return;
            }
            boolean hasMissingProductId = payload.orderProducts().stream()
                    .anyMatch(item -> FormatValidator.hasNoValue(item.productId()));
            if (hasMissingProductId) {
                publishValidationFailure(stepMessage, "productId 누락");
                return;
            }

            registerFulfillmentDelivery(payload);
            createShippingTracker(payload.orderId(), payload.buyerId());

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true}"
            );

            log.info("SAGA 풀필먼트 출고 등록 성공. sagaId={}, orderId={}", stepMessage.sagaId(), payload.orderId());
        } catch (FulfillmentDeliveryAlreadyRegisteredException e) {
            log.info("이미 출고 등록된 주문 (멱등 처리). sagaId={}, message={}", stepMessage.sagaId(), e.getMessage());
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (ShippingTrackerAlreadyExistsException e) {
            log.info("이미 배송 추적이 등록된 주문 (멱등 처리). sagaId={}, message={}",
                    stepMessage.sagaId(), e.getMessage()
            );
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"success\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 풀필먼트 출고 등록 처리 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "풀필먼트 출고 등록 처리 실패"
            );
        }
    }

    private void handleCompensation(SagaStepMessage stepMessage) {
        try {
            FulfillmentCancelSagaPayload payload = objectMapper.readValue(
                    stepMessage.payload(), FulfillmentCancelSagaPayload.class
            );

            if (FormatValidator.hasNoValue(payload.orderId())) {
                publishValidationFailure(stepMessage, "orderId 누락");
                return;
            }

            CancelInternalFulfillmentDeliveryResult result = cancelInternalFulfillmentDeliveryUseCase.cancelDelivery(
                    new CancelInternalFulfillmentDeliveryCommand(payload.orderId())
            );

            if (!result.cancelled()) {
                log.warn("SAGA 풀필먼트 출고 취소 거부. sagaId={}, orderId={}, message={}",
                        stepMessage.sagaId(), payload.orderId(), result.message()
                );
                sagaResponsePublisher.publishFailure(
                        stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                        stepMessage.messageType(), "풀필먼트 출고 취소 거부: " + result.message()
                );
                return;
            }

            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true}"
            );

            log.info("SAGA 풀필먼트 출고 취소 보상 완료. sagaId={}, orderId={}",
                    stepMessage.sagaId(), payload.orderId()
            );
        } catch (FulfillmentDeliveryRegistrationNotFoundException e) {
            log.info("출고 등록 없음 (멱등 보상 처리). sagaId={}, message={}",
                    stepMessage.sagaId(), e.getMessage()
            );
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true,\"idempotent\":true}"
            );
        } catch (FulfillmentDeliveryCancellationNotAllowedException e) {
            log.info("이미 취소된 출고 (멱등 보상 처리). sagaId={}, message={}",
                    stepMessage.sagaId(), e.getMessage()
            );
            sagaResponsePublisher.publishSuccess(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "{\"compensated\":true,\"idempotent\":true}"
            );
        } catch (Exception e) {
            log.error("SAGA 풀필먼트 출고 취소 보상 실패. sagaId={}, error={}",
                    stepMessage.sagaId(), e.getMessage(), e
            );
            sagaResponsePublisher.publishFailure(
                    stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                    stepMessage.messageType(), "풀필먼트 출고 취소 보상 처리 실패"
            );
        }
    }

    private void registerFulfillmentDelivery(FulfillmentDeliverySagaPayload payload) {
        FulfillmentAccessToken accessToken = requestFulfillmentAuthUseCase.requestAccessToken();

        RegisterFulfillmentDeliveryItemCommand itemCommand = RegisterFulfillmentDeliveryItemCommand.builder()
                .orderNumber(String.valueOf(payload.orderId()))
                .orderDate(LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE))
                .recipientName(payload.recipientName())
                .recipientPhoneNumber(payload.recipientPhoneNumber())
                .recipientAddress(buildRecipientAddress(payload))
                .shippingRequest(payload.deliveryRequestMessage())
                .products(convertToGoodsCommands(payload.orderProducts()))
                .build();

        RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                fasstoAuthProperties.getCustomerCode(), accessToken.getValue(), List.of(itemCommand)
        );

        registerFulfillmentDeliveryUseCase.registerDeliveryIdempotent(command);
    }

    private String buildRecipientAddress(FulfillmentDeliverySagaPayload payload) {
        StringBuilder address = new StringBuilder();
        if (FormatValidator.hasValue(payload.zipCode())) {
            address.append("(").append(payload.zipCode()).append(") ");
        }
        if (FormatValidator.hasValue(payload.address())) {
            address.append(payload.address());
        }
        if (FormatValidator.hasValue(payload.addressDetail())) {
            address.append(" ").append(payload.addressDetail());
        }
        return address.toString().trim();
    }

    private List<RegisterFulfillmentDeliveryGoodsCommand> convertToGoodsCommands(
            List<FulfillmentDeliverySagaPayload.OrderProductItem> orderProducts
    ) {
        return orderProducts.stream()
                .map(item -> RegisterFulfillmentDeliveryGoodsCommand.of(
                        String.valueOf(item.productId()),
                        item.quantity()
                ))
                .toList();
    }

    private void createShippingTracker(Long orderId, Long buyerId) {
        CreateShippingTrackerCommand command = new CreateShippingTrackerCommand(orderId, buyerId);
        createShippingTrackerUseCase.createShippingTracker(command);
    }

    private void publishValidationFailure(SagaStepMessage stepMessage, String reason) {
        log.warn("SAGA 풀필먼트 출고 스텝 페이로드 검증 실패. sagaId={}, reason={}", stepMessage.sagaId(), reason);
        sagaResponsePublisher.publishFailure(
                stepMessage.sagaId(), stepMessage.sagaType(), stepMessage.stepName(),
                stepMessage.messageType(), "페이로드 검증 실패: " + reason
        );
    }
}
