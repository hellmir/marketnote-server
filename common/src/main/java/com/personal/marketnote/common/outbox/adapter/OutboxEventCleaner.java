package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.OutboxProperties;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Component
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class OutboxEventCleaner {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxProperties outboxProperties;
    private final Clock clock;

    @Scheduled(cron = "${outbox.cleanup-cron:0 0 3 * * ?}")
    @Transactional(isolation = READ_COMMITTED)
    public void cleanupPublishedEvents() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(outboxProperties.getRetentionDays());
        outboxEventJpaRepository.deleteByStatusAndPublishedAtBefore(OutboxEventStatus.PUBLISHED, cutoff);
        log.info("Outbox 발행 완료 이벤트 정리 완료 - cutoff: {}", cutoff);
    }
}
