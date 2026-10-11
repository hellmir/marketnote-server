package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.CreateShippingTrackerUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedOutboundConsumer {
    private final ObjectMapper objectMapper;
    private final CreateShippingTrackerUseCase createShippingTrackerUseCase;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;
    private final FulfillmentAuthProperties fasstoAuthProperties;
    private final Clock clock;

    @KafkaListener(
            topics = KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            groupId = "fulfillment-outbound"
    )
    public void handleOrderPaymentCompletedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.ORDER_PAYMENT_COMPLETED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(OrderPaymentCompletedEvent.class, objectMapper);

            log.info("주문 결제 완료 이벤트 수신 (Fulfillment 출고 요청). eventId={}, orderId={}, orderProducts={}건",
                    envelope.eventId(), payload.orderId(),
                    FormatValidator.hasValue(payload.orderProducts()) ? payload.orderProducts().size() : 0);

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("orderId", payload.orderId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.orderProducts()) || payload.orderProducts().isEmpty()) {
                log.warn("주문 상품이 없는 이벤트 (출고 요청 생략). eventId={}, orderId={}",
                        envelope.eventId(), payload.orderId());
                acknowledgment.acknowledge();
                return;
            }

            registerFulfillmentDelivery(payload, envelope.eventId());

            createShippingTracker(payload.orderId(), payload.buyerId(), envelope.eventId());

            log.info("Fulfillment 출고 요청 이벤트 처리 완료. orderId={}, orderProducts={}건",
                    payload.orderId(), payload.orderProducts().size());
        } catch (FulfillmentDeliveryAlreadyRegisteredException e) {
            log.warn("이미 출고 등록된 주문 (멱등 처리). eventId={}, message={}",
                    envelope.eventId(), e.getMessage());
        } catch (ShippingTrackerAlreadyExistsException e) {
            log.warn("이미 배송 추적이 등록된 주문 (멱등 처리). eventId={}, orderId={}",
                    envelope.eventId(), e.getMessage());
        } catch (Exception e) {
            log.error("Fulfillment 출고 요청 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }

    private void registerFulfillmentDelivery(OrderPaymentCompletedEvent payload, String eventId) {
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

        log.info("Fassto 출고 요청 완료. eventId={}, orderId={}, products={}건",
                eventId, payload.orderId(), payload.orderProducts().size());
    }

    private String buildRecipientAddress(OrderPaymentCompletedEvent payload) {
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
            List<OrderPaymentCompletedEvent.OrderProductItem> orderProducts
    ) {
        return orderProducts.stream()
                .map(item -> RegisterFulfillmentDeliveryGoodsCommand.of(
                        String.valueOf(item.productId()),
                        item.quantity()
                ))
                .toList();
    }

    private void createShippingTracker(Long orderId, Long buyerId, String eventId) {
        CreateShippingTrackerCommand command = new CreateShippingTrackerCommand(orderId, buyerId);
        createShippingTrackerUseCase.createShippingTracker(command);
        log.info("배송 추적 생성 완료. eventId={}, orderId={}, buyerId={}", eventId, orderId, buyerId);
    }
}
