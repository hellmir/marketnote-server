package com.personal.marketnote.common.saga.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.saga.SagaSlackProperties;
import com.personal.marketnote.common.saga.port.SendSagaFailureAlertPort;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "saga", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SagaFailureSlackAlertAdapter implements SendSagaFailureAlertPort {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SagaSlackProperties sagaSlackProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendFailureAlert(
            String sagaId, String sagaType, String failedStepName,
            String reason, LocalDateTime occurredAt
    ) {
        String webhookUrl = sagaSlackProperties.getWebhookUrl();
        if (FormatValidator.hasNoValue(webhookUrl)) {
            return;
        }

        String text = "[SAGA 실패 알림] 관리자 개입 필요\n\n" +
                "• SAGA ID: " + sagaId + "\n" +
                "• SAGA 타입: " + sagaType + "\n" +
                "• 실패 스텝: " + failedStepName + "\n" +
                "• 사유: " + reason + "\n" +
                "• 감지 시각: " + occurredAt.format(DATETIME_FORMATTER);

        try {
            Map<String, String> payload = Map.of("text", text);
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            log.info("SAGA 실패 Slack 알림 전송 성공. sagaId={}, sagaType={}", sagaId, sagaType);
        } catch (Exception e) {
            log.error("SAGA 실패 Slack 알림 전송 실패. sagaId={}, sagaType={}", sagaId, sagaType, e);
        }
    }
}
