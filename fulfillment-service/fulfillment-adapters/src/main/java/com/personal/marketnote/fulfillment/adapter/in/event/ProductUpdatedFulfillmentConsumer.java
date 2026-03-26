package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FasstoAuthProperties;
import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;
import com.personal.marketnote.fulfillment.exception.FasstoAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFasstoAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFasstoGoodsUseCase;
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
public class ProductUpdatedFulfillmentConsumer {
    private final UpdateFasstoGoodsUseCase updateFasstoGoodsUseCase;
    private final RequestFasstoAuthUseCase requestFasstoAuthUseCase;
    private final FasstoAuthProperties fasstoAuthProperties;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.PRODUCT_UPDATED,
            groupId = "fulfillment-service"
    )
    public void handleProductUpdatedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_UPDATED)) {
            acknowledgment.acknowledge();
            return;
        }

        ProductUpdatedEvent payload = envelope.getPayloadAs(ProductUpdatedEvent.class, objectMapper);

        log.info("상품 수정 이벤트 수신 (풀필먼트). eventId={}, productId={}",
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
            throw new FasstoAccessTokenIssuanceFailedException(envelope.eventId(), payload.productId());
        }

        UpdateFasstoGoodsItemCommand itemCommand = UpdateFasstoGoodsItemCommand.of(
                String.valueOf(payload.productId()),
                payload.productName(),
                payload.godType(),
                payload.giftDiv(),
                payload.godOptCd1(),
                payload.godOptCd2(),
                payload.invGodNmUseYn(),
                payload.invGodNm(),
                payload.supCd(),
                payload.cateCd(),
                payload.seasonCd(),
                payload.genderCd(),
                payload.makeYr(),
                payload.godPr(),
                payload.inPr(),
                payload.salPr(),
                payload.dealTemp(),
                payload.pickFac(),
                payload.godBarcd(),
                payload.boxWeight(),
                payload.origin(),
                payload.distTermMgtYn(),
                payload.useTermDay(),
                payload.outCanDay(),
                payload.inCanDay(),
                payload.boxDiv(),
                payload.bufGodYn(),
                payload.loadingDirection(),
                payload.subMate(),
                payload.useYn(),
                payload.safetyStock(),
                payload.feeYn(),
                payload.saleUnitQty(),
                payload.cstGodImgUrl(),
                payload.externalGodImgUrl()
        );

        UpdateFasstoGoodsCommand command = UpdateFasstoGoodsCommand.of(
                fasstoAuthProperties.getCustomerCode(),
                accessToken.getValue(),
                List.of(itemCommand)
        );

        updateFasstoGoodsUseCase.updateGoods(command);

        log.info("Kafka 이벤트로 풀필먼트 상품 수정 완료. productId={}", payload.productId());
        // 예외(UpdateFasstoGoodsFailedException 포함)는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
