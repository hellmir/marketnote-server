package com.personal.marketnote.common.outbox;

public interface SaveOutboxEventPort {
    void save(OutboxEvent event);
}
