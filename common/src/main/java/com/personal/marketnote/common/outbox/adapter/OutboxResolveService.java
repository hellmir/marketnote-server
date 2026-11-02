package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveCommand;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveResult;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.exception.OutboxEventNotFoundException;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class OutboxResolveService {
    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final OutboxAuditLogger outboxAuditLogger;
    private final OutboxMetricsCollector outboxMetricsCollector;
    private final Clock clock;

    @Transactional(isolation = READ_COMMITTED)
    public OutboxResolveResult resolve(OutboxResolveCommand command) {
        OutboxEventJpaEntity entity = outboxEventJpaRepository.findById(command.id())
                .orElseThrow(() -> new OutboxEventNotFoundException(command.id()));

        if (entity.isAlreadyResolved()) {
            return new OutboxResolveResult(
                    entity.getEventId(), entity.getTopic(),
                    command.action().name(), command.reason(), true
            );
        }

        applyAction(entity, command);

        outboxAuditLogger.logResolve(entity.getTopic(), entity.getEventId(), command.action().name(), command.reason());
        outboxMetricsCollector.incrementResolvedCount(entity.getTopic(), command.action().name());

        return new OutboxResolveResult(
                entity.getEventId(), entity.getTopic(),
                command.action().name(), command.reason(), false
        );
    }

    private void applyAction(OutboxEventJpaEntity entity, OutboxResolveCommand command) {
        if (command.action().isRetry()) {
            entity.resetForRetry();
            return;
        }
        entity.discard(command.reason(), LocalDateTime.now(clock));
    }
}
