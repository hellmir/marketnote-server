package com.personal.marketnote.common.outbox.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${outbox.polling-interval-ms:3000}")
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> pendingEvents =
                outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        for (OutboxEventJpaEntity event : pendingEvents) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEventJpaEntity event) {
        try {
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);
            kafkaTemplate.send(event.getTopic(), event.getPartitionKey(), payload).get();

            event.markPublished(LocalDateTime.now(clock));
            outboxEventJpaRepository.save(event);

            log.info("Outbox 이벤트 발행 성공. topic={}, partitionKey={}, eventId={}",
                    event.getTopic(), event.getPartitionKey(), event.getEventId());
        } catch (Exception e) {
            event.incrementRetry();
            outboxEventJpaRepository.save(event);

            if (event.getStatus().isFailed()) {
                log.error("Outbox 이벤트 최대 재시도 초과. topic={}, eventId={}, retryCount={}",
                        event.getTopic(), event.getEventId(), event.getRetryCount(), e);
                return;
            }

            log.warn("Outbox 이벤트 발행 실패 (재시도 예정). topic={}, eventId={}, retryCount={}, error={}",
                    event.getTopic(), event.getEventId(), event.getRetryCount(), e.getMessage());
        }
    }
}
