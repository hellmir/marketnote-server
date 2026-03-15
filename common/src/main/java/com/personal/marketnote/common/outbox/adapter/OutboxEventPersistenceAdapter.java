package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class OutboxEventPersistenceAdapter implements SaveOutboxEventPort {
    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void save(OutboxEvent event) {
        outboxEventJpaRepository.save(OutboxEventJpaEntity.from(event));
    }
}
