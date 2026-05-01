package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
@EnableConfigurationProperties(KafkaSlackProperties.class)
public class KafkaErrorHandler {

    private static final long RETRY_INTERVAL_MS = 1000L;
    // 최초 시도 1회 + 재시도 3회 = 총 4회 시도
    private static final long RETRY_MAX_ATTEMPTS = 3L;

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    log.error("메시지 처리 실패 → DLT 전송. topic={}, key={}, offset={}",
                            record.topic(), record.key(), record.offset(), ex);
                    return new TopicPartition(
                            record.topic() + KafkaTopicConstants.DLT_SUFFIX,
                            record.partition());
                });
    }

    @Bean
    public CommonErrorHandler dltErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> log.error("DLT 메시지 최종 처리 실패 (추가 DLT 발행 없음). topic={}, key={}, offset={}",
                        record.topic(), record.key(), record.offset(), exception),
                new FixedBackOff(0L, 0L));
        return errorHandler;
    }

    @Bean
    public CommonErrorHandler commonErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        FixedBackOff backOff = new FixedBackOff(RETRY_INTERVAL_MS, RETRY_MAX_ATTEMPTS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("메시지 재시도 중. topic={}, key={}, attempt={}",
                        record.topic(), record.key(), deliveryAttempt, ex));
        return errorHandler;
    }
}
