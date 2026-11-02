package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxEventResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxTopicSummaryResponse;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class OutboxQueryService {
    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<OutboxEventResponse> queryFailedEvents(String topic) {
        if (FormatValidator.hasNoValue(topic)) {
            return outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.FAILED)
                    .stream()
                    .map(OutboxEventResponse::from)
                    .toList();
        }
        return outboxEventJpaRepository.findByStatusAndTopicOrderByCreatedAtAsc(OutboxEventStatus.FAILED, topic)
                .stream()
                .map(OutboxEventResponse::from)
                .toList();
    }

    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<OutboxTopicSummaryResponse> queryFailedSummary() {
        List<Object[]> rows = outboxEventJpaRepository.countByStatusGroupByTopic(OutboxEventStatus.FAILED);
        return rows.stream()
                .map(row -> new OutboxTopicSummaryResponse((String) row[0], (Long) row[1]))
                .toList();
    }
}
