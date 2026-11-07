package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentGoodsUseCase;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
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
    private final FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;
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

        log.info("상품 수정 이벤트 수신 (풀필먼트). eventId={}, productKey={}", envelope.eventId(), payload.productKey());

        if (FormatValidator.hasNoValue(payload.productKey())) {
            log.error("productKey 누락. eventId={}", envelope.eventId());
            acknowledgment.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productName())) {
            log.error("유효하지 않은 이벤트 페이로드. eventId={}, productName={}", envelope.eventId(), payload.productName());
            acknowledgment.acknowledge();
            return;
        }

        UpdateFulfillmentGoodsItemCommand itemCommand = UpdateFulfillmentGoodsItemCommand.builder()
                .productKey(payload.productKey())
                .productName(payload.productName())
                .productType(payload.goodsType())
                .giftDivision(payload.giftDivision())
                .productOptionCode1(payload.goodsOptionCode1())
                .productOptionCode2(payload.goodsOptionCode2())
                .inventoryProductNameUseYn(payload.invoiceGoodsNameEnabled())
                .inventoryProductName(payload.invoiceGoodsName())
                .supplierCode(payload.supplierCode())
                .categoryCode(payload.categoryCode())
                .seasonCode(payload.seasonCode())
                .genderCode(payload.genderCode())
                .manufactureYear(payload.manufactureYear())
                .productPrice(payload.unitPrice())
                .inboundPrice(payload.supplyPrice())
                .salePrice(payload.salePrice())
                .dealTemperature(payload.handlingTemperature())
                .pickFactor(payload.pickingFacility())
                .productBarcode(payload.goodsBarcode())
                .boxWeight(payload.boxWeight())
                .origin(payload.origin())
                .expirationManagementYn(payload.expirationDateManagementEnabled())
                .shelfLifeDays(payload.shelfLifeDays())
                .outboundCancelDays(payload.outboundAvailableDays())
                .inboundCancelDays(payload.inboundAvailableDays())
                .boxDivision(payload.outboundBoxType())
                .bufferProductYn(payload.cushioningEnabled())
                .loadingDirection(payload.loadingDirection())
                .subMaterial(payload.subsidiaryMaterialCode())
                .useYn(payload.enabled())
                .safetyStock(payload.safetyStock())
                .feeYn(payload.feeApplied())
                .saleUnitQuantity(payload.saleUnitQuantity())
                .customerProductImageUrl(payload.customerGoodsImageUrl())
                .externalProductImageUrl(payload.externalGoodsImageUrl())
                .build();

        fulfillmentAuthenticatedCaller.call(token -> {
            updateFulfillmentGoodsUseCase.updateGoods(
                    UpdateFulfillmentGoodsCommand.of(
                            fasstoAuthProperties.getCustomerCode(),
                            token,
                            List.of(itemCommand)
                    )
            );
            return null;
        });

        log.info("Kafka 이벤트로 풀필먼트 상품 수정 완료. productKey={}", payload.productKey());
        // 예외(UpdateFulfillmentGoodsFailedException 포함)는 DefaultErrorHandler가 재시도 + DLT로 처리

        acknowledgment.acknowledge();
    }
}
