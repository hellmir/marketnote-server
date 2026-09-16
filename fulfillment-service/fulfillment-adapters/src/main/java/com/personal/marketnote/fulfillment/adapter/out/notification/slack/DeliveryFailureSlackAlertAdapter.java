package com.personal.marketnote.fulfillment.adapter.out.notification.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.port.out.notification.SendDeliveryFailureSlackAlertPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class DeliveryFailureSlackAlertAdapter implements SendDeliveryFailureSlackAlertPort {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FulfillmentSlackProperties fulfillmentSlackProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void sendDeliveryFailureAlert(Long orderId, String trackingNumber, String carrierCode, LocalDateTime occurredAt) {
        String webhookUrl = fulfillmentSlackProperties.getWebhookUrl();
        if (FormatValidator.hasNoValue(webhookUrl)) {
            return;
        }

        String text = "[배송불가 알림] 배송불가 감지\n\n" +
                "• 주문번호: " + orderId + "\n" +
                "• 송장번호: " + trackingNumber + "\n" +
                "• 택배사코드: " + carrierCode + "\n" +
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
            log.info("배송불가 Slack 알림 전송 성공. orderId={}", orderId);
        } catch (Exception e) {
            log.error("배송불가 Slack 알림 전송 실패. orderId={}", orderId, e);
        }
    }
}
