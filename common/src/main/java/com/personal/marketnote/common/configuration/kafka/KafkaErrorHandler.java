package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class KafkaErrorHandler {

    private static final long RETRY_INITIAL_INTERVAL_MS = 1000L;
    private static final double RETRY_MULTIPLIER = 2.0;
    private static final long RETRY_MAX_ELAPSED_TIME_MS = 10000L;

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
    public CommonErrorHandler commonErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        ExponentialBackOff backOff = new ExponentialBackOff(RETRY_INITIAL_INTERVAL_MS, RETRY_MULTIPLIER);
        backOff.setMaxElapsedTime(RETRY_MAX_ELAPSED_TIME_MS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("메시지 재시도 중. topic={}, key={}, attempt={}",
                        record.topic(), record.key(), deliveryAttempt, ex));
        return errorHandler;
    }
}
