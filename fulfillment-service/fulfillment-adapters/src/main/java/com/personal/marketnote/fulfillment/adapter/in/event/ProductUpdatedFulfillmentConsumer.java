package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentAccessTokenIssuanceFailedException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
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
    private final UpdateFulfillmentGoodsUseCase updateFulfillmentGoodsUseCase;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final FulfillmentAuthProperties fasstoAuthProperties;
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

        FulfillmentAccessToken accessToken = requestFulfillmentAuthUseCase.requestAccessToken();
        if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
            throw new FulfillmentAccessTokenIssuanceFailedException(envelope.eventId(), payload.productId());
        }

        UpdateFulfillmentGoodsItemCommand itemCommand = UpdateFulfillmentGoodsItemCommand.of(
                String.valueOf(payload.productId()),
                payload.productName(),
                payload.goodsType(),
                payload.giftDivision(),
                payload.goodsOptionCode1(),
                payload.goodsOptionCode2(),
                payload.invoiceGoodsNameEnabled(),
                payload.invoiceGoodsName(),
                payload.supplierCode(),
                payload.categoryCode(),
                payload.seasonCode(),
                payload.genderCode(),
                payload.manufactureYear(),
                payload.unitPrice(),
                payload.supplyPrice(),
                payload.salePrice(),
                payload.handlingTemperature(),
                payload.pickingFacility(),
                payload.goodsBarcode(),
                payload.boxWeight(),
                payload.origin(),
                payload.expirationDateManagementEnabled(),
                payload.shelfLifeDays(),
                payload.outboundAvailableDays(),
                payload.inboundAvailableDays(),
                payload.outboundBoxType(),
                payload.cushioningEnabled(),
                payload.loadingDirection(),
                payload.subsidiaryMaterialCode(),
                payload.enabled(),
                payload.safetyStock(),
                payload.feeApplied(),
                payload.saleUnitQuantity(),
                payload.customerGoodsImageUrl(),
                payload.externalGoodsImageUrl()
        );

        UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                fasstoAuthProperties.getCustomerCode(),
                accessToken.getValue(),
                List.of(itemCommand)
        );

        updateFulfillmentGoodsUseCase.updateGoods(command);

        log.info("Kafka 이벤트로 풀필먼트 상품 수정 완료. productId={}", payload.productId());
        // 예외(UpdateFulfillmentGoodsFailedException 포함)는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
