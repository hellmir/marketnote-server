package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRegisteredFulfillmentConsumer {
    private static final String DEFAULT_GOD_TYPE = "1";
    private static final String DEFAULT_GIFT_DIV = "01";

    private final RegisterFulfillmentGoodsUseCase registerFulfillmentGoodsUseCase;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final FulfillmentAuthProperties fasstoAuthProperties;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_REGISTERED,
            groupId = "fulfillment-service"
    )
    public void handleProductRegisteredEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_REGISTERED)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            ProductRegisteredEvent payload = envelope.getPayloadAs(ProductRegisteredEvent.class, objectMapper);

            log.info("상품 등록 이벤트 수신 (풀필먼트). eventId={}, productId={}",
                    envelope.eventId(), payload.productId());

            if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                    EventPayloadValidator.id("productId", payload.productId()))) {
                acknowledgment.acknowledge();
                return;
            }

            if (FormatValidator.hasNoValue(payload.productName())) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, productName={}",
                        envelope.eventId(), payload.productName());
                acknowledgment.acknowledge();
                return;
            }

            FulfillmentAccessToken accessToken = requestFulfillmentAuthUseCase.requestAccessToken();
            if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
                throw new FulfillmentAccessTokenIssuanceFailedException(envelope.eventId(), payload.productId());
            }

            String godType = FormatValidator.hasValue(payload.goodsType()) ? payload.goodsType() : DEFAULT_GOD_TYPE;

            RegisterFulfillmentGoodsItemCommand itemCommand = RegisterFulfillmentGoodsItemCommand.builder()
                    .cstGodCd(String.valueOf(payload.productId()))
                    .godNm(payload.productName())
                    .godType(godType)
                    .giftDiv(DEFAULT_GIFT_DIV)
                    .build();

            RegisterFulfillmentGoodsCommand command = RegisterFulfillmentGoodsCommand.of(
                    fasstoAuthProperties.getCustomerCode(),
                    accessToken.getValue(),
                    List.of(itemCommand)
            );

            registerFulfillmentGoodsUseCase.registerGoodsIdempotent(command);

            log.info("Kafka 이벤트로 풀필먼트 상품 등록 완료. productId={}", payload.productId());
        } catch (FulfillmentGoodsAlreadyRegisteredException e) {
            log.warn("이미 Fulfillment에 등록된 상품입니다 (멱등 처리). eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        }
        // 그 외 예외(RegisterFulfillmentGoodsFailedException 포함)는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
