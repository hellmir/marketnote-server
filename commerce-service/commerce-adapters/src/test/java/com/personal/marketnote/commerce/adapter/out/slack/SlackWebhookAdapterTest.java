package com.personal.marketnote.commerce.adapter.out.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlackWebhookAdapter 테스트")
class SlackWebhookAdapterTest {

    @InjectMocks
    private SlackWebhookAdapter adapter;

    @Mock
    private SlackProperties slackProperties;

    @Mock
    private ObjectMapper objectMapper;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 9, 19, 0);

    @Nested
    @DisplayName("웹훅 URL이 없는 경우")
    class WhenWebhookUrlIsEmpty {

        @Test
        @DisplayName("webhookUrl이 null이면 Slack 호출을 건너뛴다")
        void shouldSkipWhenWebhookUrlIsNull() {
            adapter.sendInspectionFailedOrHoldAlert(100L, "검수 실패", NOW);

            verifyNoInteractions(objectMapper);
        }

        @Test
        @DisplayName("webhookUrl이 빈 문자열이면 Slack 호출을 건너뛴다")
        void shouldSkipWhenWebhookUrlIsEmpty() {
            adapter.sendInspectionFailedOrHoldAlert(100L, "검수 보류", NOW);

            verifyNoInteractions(objectMapper);
        }
    }
}
