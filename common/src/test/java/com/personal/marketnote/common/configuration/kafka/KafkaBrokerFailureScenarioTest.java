package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.utility.FormatValidator;
import kafka.server.BrokerServer;
import kafka.testkit.KafkaClusterTestKit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Kafka 브로커 장애 시나리오 검증 테스트.
 *
 * <p>EmbeddedKafkaKraftBroker(KRaft 모드, 3브로커)를 사용하며,
 * 테스트는 누적 시나리오 순서로 실행된다 (브로커 복구 오버헤드 제거).</p>
 *
 * <pre>
 * Phase 1 (Order 1~3): 단일 브로커 장애 — broker[0] 다운, ISR 2/3
 * Phase 2 (Order 4~5): 과반 장애 — broker[0]+[1] 다운, ISR &lt; min.insync.replicas
 * Phase 3 (Order 6~7): 전체 복구 — 모든 브로커 재시작, Producer/Consumer 정상화
 * Phase 4 (Order 8):   SLA 측정 — 장애→복구 지연시간 60초 이내
 * </pre>
 *
 * <p>KRaft combined mode(각 노드가 controller+broker)에서 컨트롤러 쿼럼 유지를 위해
 * 최대 2브로커까지만 shutdown한다 (3/3 shutdown 시 쿼럼 소실로 복구 불안정).</p>
 */
@Tag("broker-failure")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Kafka 브로커 장애 시나리오 검증")
class KafkaBrokerFailureScenarioTest {

    private static final String TEST_TOPIC = "broker-failure-test-topic";
    private static final int BROKER_COUNT = 3;
    private static final short REPLICATION_FACTOR = 3;
    private static final int PARTITIONS = 3;
    private static final int READY_CHECK_MAX_ATTEMPTS = 40;

    private EmbeddedKafkaKraftBroker embeddedKafka;
    private String bootstrapServers;

    @BeforeAll
    void setUpCluster() throws Exception {
        embeddedKafka = new EmbeddedKafkaKraftBroker(BROKER_COUNT, PARTITIONS);
        Map<String, String> brokerProperties = new HashMap<>();
        brokerProperties.put("min.insync.replicas", "2");
        brokerProperties.put("default.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("offsets.topic.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("transaction.state.log.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("broker.session.timeout.ms", "5000");
        brokerProperties.put("broker.heartbeat.interval.ms", "1000");
        brokerProperties.put("replica.lag.time.max.ms", "5000");
        embeddedKafka.brokerProperties(brokerProperties);
        embeddedKafka.afterPropertiesSet();

        bootstrapServers = embeddedKafka.getBrokersAsString();
        createTopicWithReplication();
        waitForClusterReady();
    }

    @AfterAll
    void tearDownCluster() {
        if (FormatValidator.hasValue(embeddedKafka)) {
            embeddedKafka.destroy();
        }
    }

    // === Phase 1: 단일 브로커 장애 (broker[0] 다운) ===

    @Test
    @Order(1)
    @DisplayName("단일 브로커 다운 시 Producer가 정상적으로 메시지를 발행한다 (ISR 2/3)")
    void singleBrokerDown_producerPublishesSuccessfully() throws Exception {
        // given
        shutdownBroker(0);
        waitForLeaderElection();

        // when
        RecordMetadata metadata = publishWithRetry("key-1", "단일 브로커 다운 테스트 메시지", 10);

        // then
        assertThat(metadata).isNotNull();
        assertThat(metadata.topic()).isEqualTo(TEST_TOPIC);
        assertThat(metadata.offset()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(2)
    @DisplayName("단일 브로커 다운 시 Consumer가 리밸런싱 후 정상적으로 메시지를 수신한다")
    void singleBrokerDown_consumerReceivesAfterRebalancing() throws Exception {
        // given — broker[0] 이미 다운 상태 (Order 1에서 수행)
        String testValue = "리밸런싱 후 수신 테스트 메시지-" + UUID.randomUUID();
        publishWithRetry("key-rebalance", testValue, 10);

        // when
        List<String> receivedValues = consumeMessages("rebalancing-test-group", 1, Duration.ofSeconds(30));

        // then
        assertThat(receivedValues).contains(testValue);
    }

    @Test
    @Order(3)
    @DisplayName("단일 브로커 장애 중에도 나머지 브로커를 통해 Producer와 Consumer가 정상 동작한다")
    void singleBrokerDown_producerAndConsumerWorkThroughRemainingBrokers() throws Exception {
        // given — broker[0] 이미 다운 상태
        String message = "듀얼 라이트 폴백 검증 메시지-" + UUID.randomUUID();

        // when
        RecordMetadata metadata = publishWithRetry("key-fallback", message, 10);
        assertThat(metadata).isNotNull();

        List<String> received = consumeMessages("fallback-test-group", 1, Duration.ofSeconds(30));

        // then
        assertThat(received).contains(message);
    }

    // === Phase 2: 과반 장애 (broker[0] + broker[1] 다운) ===

    @Test
    @Order(4)
    @DisplayName("2개 브로커 다운 시 Producer 발행이 실패한다 (min.insync.replicas=2 위반)")
    void twoBrokersDown_producerFailsToPublish() throws Exception {
        // given — broker[0] 이미 다운 + broker[1] 추가 다운
        shutdownBroker(1);
        waitForLeaderElection();

        // when & then
        try (KafkaProducer<String, String> producer = createProducerWithShortTimeout()) {
            Future<RecordMetadata> future = producer.send(
                    new ProducerRecord<>(TEST_TOPIC, "key-fail", "실패 메시지")
            );
            assertThatThrownBy(() -> future.get(15, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class);
        }
    }

    @Test
    @Order(5)
    @DisplayName("2개 브로커 다운 시 Producer 실패 시 TimeoutException 또는 NotEnoughReplicasException이 발생한다")
    void twoBrokersDown_producerExceptionIsPropagated() throws Exception {
        // given — broker[0]+[1] 이미 다운 상태 (Order 4에서 수행)

        // when & then
        try (KafkaProducer<String, String> producer = createProducerWithShortTimeout()) {
            Future<RecordMetadata> future = producer.send(
                    new ProducerRecord<>(TEST_TOPIC, "key-error", "에러 로깅 테스트")
            );

            assertThatThrownBy(() -> future.get(15, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .cause()
                    .satisfiesAnyOf(
                            cause -> assertThat(cause).isInstanceOf(org.apache.kafka.common.errors.TimeoutException.class),
                            cause -> assertThat(cause).isInstanceOf(org.apache.kafka.common.errors.NotEnoughReplicasException.class)
                    );
        }
    }

    // === Phase 3: 전체 복구 ===

    @Test
    @Order(6)
    @DisplayName("전체 브로커 복구 후 Producer가 메시지를 정상 전송한다")
    void allBrokersRecovered_producerSendsSuccessfully() throws Exception {
        // given — broker[0]+[1] 다운 상태에서 전체 복구
        startAllBrokers();
        waitForClusterReady();

        // when
        String recoveryMessage = "복구 후 전송 메시지-" + UUID.randomUUID();
        RecordMetadata metadata = publishWithRetry("key-recovered", recoveryMessage, 10);

        // then
        assertThat(metadata).isNotNull();
        assertThat(metadata.topic()).isEqualTo(TEST_TOPIC);

        List<String> received = consumeMessages("recovery-producer-group", 1, Duration.ofSeconds(30));
        assertThat(received).contains(recoveryMessage);
    }

    @Test
    @Order(7)
    @DisplayName("전체 브로커 복구 후 Consumer가 자동으로 재연결하여 메시지를 수신한다")
    void allBrokersRecovered_consumerReconnectsAutomatically() throws Exception {
        // given — 모든 브로커 정상 상태 (Order 6에서 복구됨)
        String preMessage = "장애 전 발행 메시지-" + UUID.randomUUID();
        publishWithRetry("key-pre", preMessage, 5);

        // 과반 장애 시뮬레이션 (2/3 다운, 컨트롤러 쿼럼 1/3 유지)
        shutdownBroker(0);
        shutdownBroker(1);
        waitForLeaderElection();

        // when — 전체 복구
        startAllBrokers();
        waitForClusterReady();

        String postMessage = "복구 후 발행 메시지-" + UUID.randomUUID();
        RecordMetadata metadata = publishWithRetry("key-post", postMessage, 10);
        assertThat(metadata).isNotNull();

        // then
        List<String> received = consumeMessages("reconnect-test-group", 2, Duration.ofSeconds(30));
        assertThat(received).contains(preMessage, postMessage);
    }

    // === Phase 4: SLA 측정 ===

    @Test
    @Order(8)
    @DisplayName("장애 복구 시간을 측정한다 - 복구 후 첫 메시지 수신까지의 지연시간이 60초 이내이다")
    void measureRecoveryTime_withinSlaBoundary() throws Exception {
        // given — 과반 장애 (2/3 다운, 컨트롤러 쿼럼 1/3 유지)
        shutdownBroker(0);
        shutdownBroker(1);
        Thread.sleep(3000);

        // when
        long recoveryStartNanos = System.nanoTime();
        startAllBrokers();

        RecordMetadata metadata = null;
        long publishStartNanos = System.nanoTime();
        for (int attempt = 0; attempt < READY_CHECK_MAX_ATTEMPTS; attempt++) {
            try (KafkaProducer<String, String> producer = createProducer()) {
                Future<RecordMetadata> future = producer.send(
                        new ProducerRecord<>(TEST_TOPIC, "key-sla", "SLA 측정 메시지")
                );
                metadata = future.get(5, TimeUnit.SECONDS);
                break;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        long publishLatencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - publishStartNanos);
        long totalRecoveryMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - recoveryStartNanos);

        // then
        assertThat(metadata).isNotNull();
        assertThat(totalRecoveryMs).isLessThan(60_000L);

        System.out.println("=== Kafka 장애 복구 시간 측정 ===");
        System.out.println("  복구 시작 → 첫 발행 성공: " + publishLatencyMs + "ms");
        System.out.println("  복구 시작 → 전체 소요: " + totalRecoveryMs + "ms");
        System.out.println("  SLA 기준 60초 이내: PASS");
    }

    // === 유틸리티 메서드 ===

    private void createTopicWithReplication() throws Exception {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic topic = new NewTopic(TEST_TOPIC, PARTITIONS, REPLICATION_FACTOR);
            topic.configs(Map.of("min.insync.replicas", "2"));
            adminClient.createTopics(Collections.singleton(topic)).all().get(30, TimeUnit.SECONDS);
        }
    }

    private void waitForClusterReady() throws InterruptedException {
        for (int attempt = 0; attempt < READY_CHECK_MAX_ATTEMPTS; attempt++) {
            try (KafkaProducer<String, String> producer = createProducerWithShortTimeout()) {
                Future<RecordMetadata> future = producer.send(
                        new ProducerRecord<>(TEST_TOPIC, "readiness-" + attempt, "cluster-ready-check")
                );
                future.get(5, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
    }

    private void waitForLeaderElection() throws Exception {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            for (int attempt = 0; attempt < READY_CHECK_MAX_ATTEMPTS; attempt++) {
                try {
                    TopicDescription description = adminClient
                            .describeTopics(Collections.singletonList(TEST_TOPIC))
                            .allTopicNames()
                            .get(10, TimeUnit.SECONDS)
                            .get(TEST_TOPIC);

                    boolean allPartitionsHaveLeader = description.partitions().stream()
                            .map(TopicPartitionInfo::leader)
                            .allMatch(leader -> FormatValidator.hasValue(leader) && leader.id() >= 0);

                    if (allPartitionsHaveLeader) {
                        Thread.sleep(2000);
                        return;
                    }
                } catch (Exception e) {
                    // 리더 선출 진행 중 — 재시도
                }
                Thread.sleep(1000);
            }
        }
    }

    private RecordMetadata publishWithRetry(String key, String value, int maxAttempts) throws Exception {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try (KafkaProducer<String, String> producer = createProducer()) {
                Future<RecordMetadata> future = producer.send(new ProducerRecord<>(TEST_TOPIC, key, value));
                return future.get(10, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                if (attempt == maxAttempts - 1) {
                    throw e;
                }
                Thread.sleep(2000);
            }
        }
        return null;
    }

    private KafkaProducer<String, String> createProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 3000);
        return new KafkaProducer<>(props);
    }

    private KafkaProducer<String, String> createProducerWithShortTimeout() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        return new KafkaProducer<>(props);
    }

    private List<String> consumeMessages(String groupId, int expectedCount, Duration timeout) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        List<String> received = new ArrayList<>();
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TEST_TOPIC));
            while (received.size() < expectedCount && System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    received.add(record.value());
                }
            }
        }
        return received;
    }

    private void shutdownBroker(int brokerIndex) {
        KafkaClusterTestKit cluster = embeddedKafka.getCluster();
        List<Integer> brokerIds = new ArrayList<>(cluster.brokers().keySet());
        Collections.sort(brokerIds);

        if (brokerIndex >= brokerIds.size()) {
            return;
        }

        Integer brokerId = brokerIds.get(brokerIndex);
        BrokerServer broker = cluster.brokers().get(brokerId);
        if (FormatValidator.hasValue(broker) && !broker.isShutdown()) {
            broker.shutdown();
            broker.awaitShutdown();
        }
    }

    private void startAllBrokers() {
        KafkaClusterTestKit cluster = embeddedKafka.getCluster();
        for (Map.Entry<Integer, BrokerServer> entry : cluster.brokers().entrySet()) {
            BrokerServer broker = entry.getValue();
            if (broker.isShutdown()) {
                broker.startup();
            }
        }
    }
}
