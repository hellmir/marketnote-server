package com.personal.marketnote.common.configuration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltSlackNotifier {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final KafkaSlackProperties kafkaSlackProperties;
    private final ObjectMapper objectMapper;

    public void notify(
            String originalTopic,
            ConsumerRecord<String, Object> record,
            String errorFqcn,
            String errorMessage
    ) {
        String webhookUrl = kafkaSlackProperties.getWebhookUrl();
        if (FormatValidator.hasNoValue(webhookUrl)) {
            return;
        }

        String text = "[DLT Alert] Kafka 메시지 처리 실패\n\n" +
                "• 원본 토픽: " + originalTopic + "\n" +
                "• DLT 토픽: " + record.topic() + "\n" +
                "• 파티션: " + record.partition() + "\n" +
                "• 오프셋: " + record.offset() + "\n" +
                "• 이벤트 키: " + record.key() + "\n" +
                "• 에러 유형: " + errorFqcn + "\n" +
                "• 에러 메시지: " + errorMessage;

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
            log.info("Slack DLT 알림 전송 성공. originalTopic={}", originalTopic);
        } catch (Exception e) {
            log.error("Slack webhook 호출 실패. originalTopic={}", originalTopic, e);
        }
    }
}
