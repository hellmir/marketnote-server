package com.personal.marketnote.common.configuration.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DltHeaderExtractor 테스트")
class DltHeaderExtractorTest {

    @Test
    @DisplayName("DLT 헤더에서 원본 토픽을 추출한다")
    void extractOriginalTopic_returnsHeaderValue() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic.dlt", 0, 0L, "key", "value");
        record.headers().add("kafka_dlt-original-topic", "commerce.order.payment-completed".getBytes(StandardCharsets.UTF_8));

        // when
        String result = DltHeaderExtractor.extractOriginalTopic(record);

        // then
        assertThat(result).isEqualTo("commerce.order.payment-completed");
    }

    @Test
    @DisplayName("DLT 헤더에서 예외 FQCN을 추출한다")
    void extractExceptionFqcn_returnsHeaderValue() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic.dlt", 0, 0L, "key", "value");
        record.headers().add("kafka_dlt-exception-fqcn", "java.lang.RuntimeException".getBytes(StandardCharsets.UTF_8));

        // when
        String result = DltHeaderExtractor.extractExceptionFqcn(record);

        // then
        assertThat(result).isEqualTo("java.lang.RuntimeException");
    }

    @Test
    @DisplayName("DLT 헤더에서 예외 메시지를 추출한다")
    void extractExceptionMessage_returnsHeaderValue() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic.dlt", 0, 0L, "key", "value");
        record.headers().add("kafka_dlt-exception-message", "DB 연결 오류".getBytes(StandardCharsets.UTF_8));

        // when
        String result = DltHeaderExtractor.extractExceptionMessage(record);

        // then
        assertThat(result).isEqualTo("DB 연결 오류");
    }

    @Test
    @DisplayName("DLT 헤더가 없으면 UNKNOWN을 반환한다")
    void extractOriginalTopic_noHeader_returnsUnknown() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic.dlt", 0, 0L, "key", "value");

        // when
        String result = DltHeaderExtractor.extractOriginalTopic(record);

        // then
        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("모든 DLT 헤더가 없으면 모두 UNKNOWN을 반환한다")
    void extractAll_noHeaders_allReturnUnknown() {
        // given
        ConsumerRecord<String, Object> record = new ConsumerRecord<>("topic.dlt", 0, 0L, "key", "value");

        // when & then
        assertThat(DltHeaderExtractor.extractOriginalTopic(record)).isEqualTo("UNKNOWN");
        assertThat(DltHeaderExtractor.extractExceptionFqcn(record)).isEqualTo("UNKNOWN");
        assertThat(DltHeaderExtractor.extractExceptionMessage(record)).isEqualTo("UNKNOWN");
    }
}
