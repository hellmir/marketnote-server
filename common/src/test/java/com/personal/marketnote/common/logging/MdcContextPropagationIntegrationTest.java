package com.personal.marketnote.common.logging;

import com.personal.marketnote.common.utility.FormatValidator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.MDC;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("MDC 컨텍스트 전파 통합")
class MdcContextPropagationIntegrationTest {

    private static final String TEST_TOPIC = "mdc-propagation-test-topic";

    private EmbeddedKafkaKraftBroker embeddedKafka;
    private String bootstrapServers;
    private final KafkaMdcProducerInterceptor producerInterceptor = new KafkaMdcProducerInterceptor();
    private final KafkaMdcRecordInterceptor recordInterceptor = new KafkaMdcRecordInterceptor();

    @BeforeAll
    void setUp() throws Exception {
        embeddedKafka = new EmbeddedKafkaKraftBroker(1, 1, TEST_TOPIC);
        embeddedKafka.afterPropertiesSet();
        bootstrapServers = embeddedKafka.getBrokersAsString();
    }

    @AfterAll
    void tearDown() {
        if (FormatValidator.hasValue(embeddedKafka)) {
            embeddedKafka.destroy();
        }
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Producer 발행 시 MDC traceId가 헤더로 전파되어 Consumer MDC에 동일하게 복원된다")
    void traceIdPropagatedFromProducerMdcToConsumerMdc() throws Exception {
        // given — Producer 측 진입 시 MDC 설정 (HTTP 진입 시뮬)
        String entryTraceId = "entry-" + UUID.randomUUID();
        MDC.put(TraceIdMdcFilter.TRACE_ID_MDC_KEY, entryTraceId);

        // when — Producer 인터셉터 적용해 send
        Map<String, Object> producerProps = producerProps();
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(TEST_TOPIC, "key", "value");
            ProducerRecord<String, String> intercepted = applyInterceptor(record);
            producer.send(intercepted).get(10, TimeUnit.SECONDS);
        }

        // Consumer 측 — 인터셉터가 헤더에서 MDC 복원
        MDC.clear();
        AtomicReference<String> consumerTraceId = new AtomicReference<>();
        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps("propagation-group"))) {
            consumer.subscribe(Collections.singletonList(TEST_TOPIC));
            long deadline = System.currentTimeMillis() + 15_000L;
            while (System.currentTimeMillis() < deadline && FormatValidator.hasNoValue(consumerTraceId.get())) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, Object> consumed : records) {
                    recordInterceptor.intercept(consumed, consumer);
                    consumerTraceId.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY));
                    recordInterceptor.afterRecord(consumed, consumer);
                }
            }
        }

        // then
        assertThat(consumerTraceId.get()).isEqualTo(entryTraceId);
    }

    @Test
    @DisplayName("Producer 발행 시 MDC가 비어있으면 Consumer는 신규 traceId를 생성한다")
    void consumerGeneratesNewTraceIdWhenProducerMdcEmpty() throws Exception {
        // given — Producer 측 MDC 비움
        MDC.clear();

        // when
        Map<String, Object> producerProps = producerProps();
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(TEST_TOPIC, "key2", "value2");
            ProducerRecord<String, String> intercepted = applyInterceptor(record);
            producer.send(intercepted).get(10, TimeUnit.SECONDS);
        }

        AtomicReference<String> consumerTraceId = new AtomicReference<>();
        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps("propagation-group-2"))) {
            consumer.subscribe(Collections.singletonList(TEST_TOPIC));
            long deadline = System.currentTimeMillis() + 15_000L;
            while (System.currentTimeMillis() < deadline && FormatValidator.hasNoValue(consumerTraceId.get())) {
                ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, Object> consumed : records) {
                    if (!"key2".equals(consumed.key())) {
                        continue;
                    }
                    recordInterceptor.intercept(consumed, consumer);
                    consumerTraceId.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY));
                    recordInterceptor.afterRecord(consumed, consumer);
                }
            }
        }

        // then
        assertThat(consumerTraceId.get()).isNotNull();
        assertThat(consumerTraceId.get().length()).isEqualTo(36);
    }

    @Test
    @DisplayName("MdcTaskDecorator 적용된 TaskScheduler가 부모 MDC 부재 시 신규 traceId를 생성한다")
    void schedulerGeneratesNewTraceIdWhenParentMdcEmpty() throws Exception {
        // given
        MdcTaskDecorator decorator = new MdcTaskDecorator();
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("test-scheduler-");
        scheduler.setTaskDecorator(decorator);
        scheduler.initialize();

        try {
            MDC.clear();
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> capturedTraceId = new AtomicReference<>();

            // when
            scheduler.schedule(() -> {
                capturedTraceId.set(MDC.get(TraceIdMdcFilter.TRACE_ID_MDC_KEY));
                latch.countDown();
            }, Instant.now().plusMillis(50));

            boolean completed = latch.await(5, TimeUnit.SECONDS);

            // then
            assertThat(completed).isTrue();
            assertThat(capturedTraceId.get()).isNotNull();
            assertThat(capturedTraceId.get().length()).isEqualTo(36);
        } finally {
            scheduler.shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    private ProducerRecord<String, String> applyInterceptor(ProducerRecord<String, String> record) {
        ProducerRecord<String, Object> casted = new ProducerRecord<>(
                record.topic(),
                record.partition(),
                record.timestamp(),
                record.key(),
                record.value(),
                record.headers()
        );
        ProducerRecord<String, Object> result = producerInterceptor.onSend(casted);
        return new ProducerRecord<>(
                result.topic(),
                result.partition(),
                result.timestamp(),
                result.key(),
                (String) result.value(),
                result.headers()
        );
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return props;
    }

    private Map<String, Object> consumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }
}
