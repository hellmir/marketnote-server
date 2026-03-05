package com.personal.marketnote.product.adapter.out.event;

import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ProductEventKafkaProducer implements PublishProductEventPort {
    private static final String SOURCE = "product-service";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    @Override
    public void publishProductRegisteredEvent(Long productId, Long pricePolicyId, Long sellerId) {
        ProductRegisteredEvent payload = new ProductRegisteredEvent(productId, pricePolicyId, sellerId);
        EventEnvelope<ProductRegisteredEvent> envelope = EventEnvelope.of(
                KafkaTopicConstants.PRODUCT_REGISTERED,
                SOURCE,
                payload,
                clock
        );

        // TODO: Kafka 단독 전환 시 발행 실패 처리 보강 필요 (Outbox 패턴 또는 동기 전환)
        kafkaTemplate.send(KafkaTopicConstants.PRODUCT_REGISTERED, productId.toString(), envelope)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 이벤트 발행 실패. topic={}, productId={}, pricePolicyId={}, sellerId={}",
                                KafkaTopicConstants.PRODUCT_REGISTERED, productId, pricePolicyId, sellerId, ex);
                    } else {
                        log.info("Kafka 이벤트 발행 성공. topic={}, productId={}, pricePolicyId={}, sellerId={}, offset={}",
                                KafkaTopicConstants.PRODUCT_REGISTERED, productId, pricePolicyId, sellerId,
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
