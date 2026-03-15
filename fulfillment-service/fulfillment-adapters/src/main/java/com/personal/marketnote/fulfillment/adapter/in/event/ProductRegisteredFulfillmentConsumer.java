package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;
import com.personal.marketnote.fulfillment.exception.FasstoGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.exception.RegisterFasstoGoodsFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFasstoGoodsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFasstoAuthUseCase;
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

    private final RegisterFasstoGoodsUseCase registerFasstoGoodsUseCase;
    private final RequestFasstoAuthUseCase requestFasstoAuthUseCase;
    private final FasstoAuthProperties fasstoAuthProperties;
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

            FasstoAccessToken accessToken = requestFasstoAuthUseCase.requestAccessToken();
            if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
                log.error("Fassto 액세스 토큰 발급 실패. eventId={}, productId={}",
                        envelope.eventId(), payload.productId());
                acknowledgment.acknowledge();
                return;
            }

            String godType = FormatValidator.hasValue(payload.godType()) ? payload.godType() : DEFAULT_GOD_TYPE;

            RegisterFasstoGoodsItemCommand itemCommand = RegisterFasstoGoodsItemCommand.of(
                    String.valueOf(payload.productId()),
                    payload.productName(),
                    godType,
                    DEFAULT_GIFT_DIV,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null
            );

            RegisterFasstoGoodsCommand command = RegisterFasstoGoodsCommand.of(
                    fasstoAuthProperties.getCustomerCode(),
                    accessToken.getValue(),
                    List.of(itemCommand)
            );

            registerFasstoGoodsUseCase.registerGoodsIdempotent(command);

            log.info("Kafka 이벤트로 풀필먼트 상품 등록 완료. productId={}", payload.productId());
        } catch (FasstoGoodsAlreadyRegisteredException e) {
            log.warn("이미 Fassto에 등록된 상품입니다 (멱등 처리). eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        } catch (RegisterFasstoGoodsFailedException e) {
            log.warn("풀필먼트 상품 등록 실패 (듀얼 라이트 기간 HTTP 호출로 처리됨). eventId={}, key={}, message={}",
                    envelope.eventId(), record.key(), e.getMessage());
        }
        // 그 외 예외는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
