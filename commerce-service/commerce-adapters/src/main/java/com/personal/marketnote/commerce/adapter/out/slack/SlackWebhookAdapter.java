package com.personal.marketnote.commerce.adapter.out.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.out.slack.SendSlackAlertPort;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackWebhookAdapter implements SendSlackAlertPort {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final SlackProperties slackProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendInspectionFailedOrHoldAlert(Long orderId, String inspectionStatus, LocalDateTime inspectedAt) {
        String webhookUrl = slackProperties.getWebhookUrl();
        if (FormatValidator.hasNoValue(webhookUrl)) {
            return;
        }

        String text = """
                [검수 알림] 반품 검수 불량/보류 발생

                • orderId: %d
                • 검수 상태: %s
                • 검수 시각: %s""".formatted(orderId, inspectionStatus, inspectedAt);

        try {
            Map<String, String> payload = Map.of("text", text);
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> log.info("Slack 검수 알림 전송 성공. orderId={}", orderId))
                    .exceptionally(ex -> {
                        log.warn("Slack 검수 알림 전송 실패. orderId={}", orderId, ex);
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Slack 검수 알림 메시지 생성 실패. orderId={}", orderId, e);
        }
    }
}
