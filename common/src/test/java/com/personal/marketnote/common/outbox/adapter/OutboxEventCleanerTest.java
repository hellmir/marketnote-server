package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.OutboxProperties;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventCleaner 테스트")
class OutboxEventCleanerTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private OutboxEventCleaner outboxEventCleaner;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Mock
    private OutboxProperties outboxProperties;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("보존기간이 지난 PUBLISHED 이벤트를 삭제한다")
    void cleanupPublishedEvents_deletesPublishedEventsOlderThanRetentionDays() {
        // given
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
        when(outboxProperties.getRetentionDays()).thenReturn(7);

        LocalDateTime expectedCutoff = LocalDateTime.now(FIXED_CLOCK).minusDays(7);

        // when
        outboxEventCleaner.cleanupPublishedEvents();

        // then
        verify(outboxEventJpaRepository).deleteByStatusAndPublishedAtBefore(OutboxEventStatus.PUBLISHED, expectedCutoff);
    }
}
