package com.personal.marketnote.fulfillment.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentCompletedOutboundConsumer {
    private final ObjectMapper objectMapper;

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
            OrderPaymentCompletedEvent payload = envelope.getPayloadAs(
                    OrderPaymentCompletedEvent.class, objectMapper
            );

            log.info("주문 결제 완료 이벤트 수신 (Fassto 출고 요청). eventId={}, orderId={}, orderProducts={}건",
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

            // FIXME: [#929][#1200] Fassto 출고 요청 구현 필요
            //  신규 기능 (기존 HTTP 호출 없음). 배송지 정보 추가 후 아래 코드 활성화:
            //
            //  FasstoAccessToken accessToken = requestFasstoAuthUseCase.requestAccessToken();
            //  RegisterFasstoDeliveryItemCommand itemCommand = RegisterFasstoDeliveryItemCommand.builder()
            //          .ordNo(String.valueOf(payload.orderId()))
            //          .ordDt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
            //          .godCds(convertToGoodsCommands(payload.orderProducts()))
            //          .build();
            //  RegisterFasstoDeliveryCommand command = RegisterFasstoDeliveryCommand.of(
            //          fasstoAuthProperties.getCustomerCode(), accessToken.getValue(), List.of(itemCommand)
            //  );
            //  registerFasstoDeliveryUseCase.registerDeliveryIdempotent(command);
            //
            //  [#1216] 멱등성 보강 완료: registerDeliveryIdempotent() 사용
            //  — orderId 기반 출고 이력 DB 저장 (UNIQUE 제약) → 중복 시 FasstoDeliveryAlreadyRegisteredException
            //  — Consumer에서 FasstoDeliveryAlreadyRegisteredException catch → warn + acknowledge

            log.info("Fassto 출고 요청 이벤트 검증 완료. orderId={}, orderProducts={}건",
                    payload.orderId(), payload.orderProducts().size());
        } catch (Exception e) {
            log.error("Fassto 출고 요청 이벤트 처리 실패. eventId={}, key={}, error={}",
                    envelope.eventId(), record.key(), e.getMessage(), e);
            throw e;
        }

        acknowledgment.acknowledge();
    }
}
