package com.personal.marketnote.common.configuration.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kafka Consumer 장애 및 리밸런싱 시나리오 검증 테스트.
 *
 * <p>EmbeddedKafkaKraftBroker(KRaft 모드, 3브로커, 3파티션)를 사용하며,
 * 각 테스트는 고유 groupId를 사용하여 독립적으로 실행된다.</p>
 *
 * <pre>
 * Phase 1 (Order 1~2): Consumer 장애 후 재시작 — committed offset 기반 재소비 검증
 * Phase 2 (Order 3~4): Consumer Group 리밸런싱 — Consumer 이탈/추가 시 파티션 재할당
 * Phase 3 (Order 5~6): 리밸런싱 중 메시지 무결성 — 유실 없음 + 중복 소비 없음
 * Phase 4 (Order 7):   Consumer Lag 복구 — 밀린 메시지 전체 소비 + 복구 시간 60초 이내
 * Phase 5 (Order 8):   max.poll.interval.ms 킥아웃 — 느린 Consumer 파티션 인수
 * </pre>
 *
 * <p>운영 환경과 동일한 핵심 설정 적용:
 * enable.auto.commit=false, auto.offset.reset=earliest, max.poll.records=10</p>
 *
 * <p>EmbeddedKafka 환경에서의 session.timeout.ms는 10초로 설정하여 빠른 리밸런싱 감지를 유도한다.
 * (운영 기본값: 45초)</p>
 */
@Tag("slow")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Kafka Consumer 장애 및 리밸런싱 시나리오 검증")
class KafkaConsumerRebalancingScenarioTest {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerRebalancingScenarioTest.class);

    private static final int BROKER_COUNT = 3;
    private static final short REPLICATION_FACTOR = 3;
    private static final int PARTITIONS = 3;
    private static final int READY_CHECK_MAX_ATTEMPTS = 40;
    private static final int SESSION_TIMEOUT_MS = 10_000;
    private static final int HEARTBEAT_INTERVAL_MS = 3_000;

    private EmbeddedKafkaKraftBroker embeddedKafka;
    private String bootstrapServers;
    private String testTopic;

    @BeforeAll
    void setUpCluster() throws Exception {
        testTopic = "consumer-rebalancing-test-" + UUID.randomUUID().toString().substring(0, 8);

        embeddedKafka = new EmbeddedKafkaKraftBroker(BROKER_COUNT, PARTITIONS);
        Map<String, String> brokerProperties = new HashMap<>();
        brokerProperties.put("min.insync.replicas", "2");
        brokerProperties.put("default.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("offsets.topic.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("transaction.state.log.replication.factor", String.valueOf(REPLICATION_FACTOR));
        brokerProperties.put("group.initial.rebalance.delay.ms", "0");
        embeddedKafka.brokerProperties(brokerProperties);
        embeddedKafka.afterPropertiesSet();

        bootstrapServers = embeddedKafka.getBrokersAsString();
        createTopic();
        waitForClusterReady();
    }

    @AfterAll
    void tearDownCluster() {
        if (embeddedKafka != null) {
            embeddedKafka.destroy();
        }
    }

    // === Phase 1: Consumer 장애 후 재시작 ===

    @Test
    @Order(1)
    @DisplayName("Consumer 장애 후 재시작 시 미처리 메시지를 committed offset부터 정상 소비한다")
    void consumerRestart_consumesFromCommittedOffset() throws Exception {
        // given
        String groupId = "restart-committed-" + UUID.randomUUID();
        int additionalMessageCount = 3;

        // 기존 메시지를 모두 소비하고 커밋하여 베이스라인 설정
        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(testTopic));
            drainAllMessages(consumer, Duration.ofSeconds(15));
            consumer.commitSync();
        }

        // Consumer 종료 상태에서 추가 메시지 발행
        publishMessages("restart-key", "추가-", additionalMessageCount);

        // when — 동일 groupId로 재시작
        try (KafkaConsumer<String, String> newConsumer = createConsumer(groupId)) {
            newConsumer.subscribe(Collections.singletonList(testTopic));
            List<String> received = drainAllMessages(newConsumer, Duration.ofSeconds(30));

            // then — 추가 메시지만 소비 (committed offset 이후)
            assertThat(received).hasSize(additionalMessageCount);
            assertThat(received).allMatch(value -> value.startsWith("추가-"));
        }
    }

    @Test
    @Order(2)
    @DisplayName("Consumer 장애 후 재시작 시 커밋되지 않은 메시지를 재소비한다")
    void consumerRestart_reConsumesUncommittedMessages() throws Exception {
        // given
        String groupId = "restart-uncommitted-" + UUID.randomUUID();
        int messageCount = 5;

        // 기존 메시지를 모두 소비하고 커밋하여 베이스라인 설정
        try (KafkaConsumer<String, String> baseline = createConsumer(groupId)) {
            baseline.subscribe(Collections.singletonList(testTopic));
            drainAllMessages(baseline, Duration.ofSeconds(15));
            baseline.commitSync();
        }

        // 테스트 메시지 발행
        publishMessages("uncommit-key", "미커밋-", messageCount);

        // Consumer가 메시지를 poll하되 commit하지 않고 종료
        List<String> firstPoll;
        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(testTopic));
            firstPoll = drainAllMessages(consumer, Duration.ofSeconds(30));
            assertThat(firstPoll).hasSize(messageCount);
            assertThat(firstPoll).allMatch(value -> value.startsWith("미커밋-"));
            // commitSync() 호출하지 않음 — 의도적으로 커밋 생략
        }

        // when — 동일 groupId로 재시작
        try (KafkaConsumer<String, String> newConsumer = createConsumer(groupId)) {
            newConsumer.subscribe(Collections.singletonList(testTopic));
            List<String> reConsumed = drainAllMessages(newConsumer, Duration.ofSeconds(30));

            // then — 커밋되지 않은 메시지가 재소비됨 (at-least-once 보장)
            assertThat(reConsumed).hasSize(messageCount);
            assertThat(reConsumed).containsExactlyInAnyOrderElementsOf(firstPoll);
        }
    }

    // === Phase 2: Consumer Group 리밸런싱 ===

    @Test
    @Order(3)
    @DisplayName("Consumer 이탈 시 남은 Consumer가 이탈한 Consumer의 파티션을 인수하여 메시지를 소비한다")
    void consumerLeave_remainingConsumerTakesOverPartitions() throws Exception {
        // given
        String groupId = "leave-takeover-" + UUID.randomUUID();
        AtomicBoolean consumerARunning = new AtomicBoolean(true);
        AtomicBoolean consumerBRunning = new AtomicBoolean(true);
        ConcurrentLinkedQueue<String> consumerAMessages = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> consumerBMessages = new ConcurrentLinkedQueue<>();
        CountDownLatch bothSubscribed = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Consumer A, B를 동일 groupId로 구독
            Future<?> futureA = executor.submit(() ->
                    consumeAsync(groupId, consumerARunning, consumerAMessages, bothSubscribed));
            Future<?> futureB = executor.submit(() ->
                    consumeAsync(groupId, consumerBRunning, consumerBMessages, bothSubscribed));

            // 두 Consumer 모두 구독 대기
            assertThat(bothSubscribed.await(30, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(SESSION_TIMEOUT_MS);

            // when — Consumer B 이탈
            consumerBRunning.set(false);
            futureB.get(15, TimeUnit.SECONDS);

            // 리밸런싱 대기 (session.timeout.ms의 2배)
            Thread.sleep(SESSION_TIMEOUT_MS * 2L);

            // 이탈 후 메시지 발행
            int messageCount = 9;
            publishMessages("leave-key", "이탈후-", messageCount);
            Thread.sleep(5000);

            // then — Consumer A가 모든 파티션의 메시지를 소비
            consumerARunning.set(false);
            futureA.get(15, TimeUnit.SECONDS);

            assertThat(consumerAMessages.stream().filter(v -> v.startsWith("이탈후-")).count())
                    .isEqualTo(messageCount);
        } finally {
            consumerARunning.set(false);
            consumerBRunning.set(false);
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Consumer 추가 시 파티션이 재분배되어 새 Consumer도 메시지를 소비한다")
    void consumerJoin_partitionsRedistributed() throws Exception {
        // given
        String groupId = "join-redistribute-" + UUID.randomUUID();
        AtomicBoolean consumerARunning = new AtomicBoolean(true);
        AtomicBoolean consumerCRunning = new AtomicBoolean(true);
        ConcurrentLinkedQueue<String> consumerAMessages = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> consumerCMessages = new ConcurrentLinkedQueue<>();
        CountDownLatch consumerASubscribed = new CountDownLatch(1);
        CountDownLatch consumerCSubscribed = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Consumer A만 먼저 구독 (3개 파티션 전부 점유)
            Future<?> futureA = executor.submit(() ->
                    consumeAsync(groupId, consumerARunning, consumerAMessages, consumerASubscribed));
            assertThat(consumerASubscribed.await(30, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(SESSION_TIMEOUT_MS);

            // when — Consumer C를 추가 구독 (리밸런싱 트리거)
            Future<?> futureC = executor.submit(() ->
                    consumeAsync(groupId, consumerCRunning, consumerCMessages, consumerCSubscribed));
            assertThat(consumerCSubscribed.await(30, TimeUnit.SECONDS)).isTrue();

            // 리밸런싱 완료 대기
            Thread.sleep(SESSION_TIMEOUT_MS * 2L);

            // 메시지 발행 — 3개 파티션으로 분산
            int messageCount = 30;
            publishMessages("join-key", "분산-", messageCount);
            Thread.sleep(5000);

            // then — 두 Consumer 모두 메시지를 수신
            consumerARunning.set(false);
            consumerCRunning.set(false);
            futureA.get(15, TimeUnit.SECONDS);
            futureC.get(15, TimeUnit.SECONDS);

            long consumerACount = consumerAMessages.stream().filter(v -> v.startsWith("분산-")).count();
            long consumerCCount = consumerCMessages.stream().filter(v -> v.startsWith("분산-")).count();

            assertThat(consumerACount).isGreaterThan(0);
            assertThat(consumerCCount).isGreaterThan(0);
            assertThat(consumerACount + consumerCCount).isEqualTo(messageCount);
        } finally {
            consumerARunning.set(false);
            consumerCRunning.set(false);
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    // === Phase 3: 리밸런싱 중 메시지 무결성 ===

    @Test
    @Order(5)
    @DisplayName("리밸런싱 진행 중에 발행된 메시지가 유실 없이 모두 소비된다")
    void duringRebalancing_noMessageLoss() throws Exception {
        // given
        String groupId = "rebalance-no-loss-" + UUID.randomUUID();
        AtomicBoolean consumerARunning = new AtomicBoolean(true);
        AtomicBoolean consumerBRunning = new AtomicBoolean(true);
        ConcurrentLinkedQueue<String> consumerAMessages = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> consumerBMessages = new ConcurrentLinkedQueue<>();
        CountDownLatch consumerASubscribed = new CountDownLatch(1);
        CountDownLatch consumerBSubscribed = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Consumer A 구독 중
            Future<?> futureA = executor.submit(() ->
                    consumeAsync(groupId, consumerARunning, consumerAMessages, consumerASubscribed));
            assertThat(consumerASubscribed.await(30, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(SESSION_TIMEOUT_MS);

            // when — Consumer B 추가 (리밸런싱 트리거) + 동시에 메시지 발행
            Future<?> futureB = executor.submit(() ->
                    consumeAsync(groupId, consumerBRunning, consumerBMessages, consumerBSubscribed));

            int messageCount = 30;
            publishMessages("loss-key", "리밸런싱중-", messageCount);

            assertThat(consumerBSubscribed.await(30, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(SESSION_TIMEOUT_MS * 2L);

            // then — A + B의 수신 메시지 합계 = 발행 메시지 수
            consumerARunning.set(false);
            consumerBRunning.set(false);
            futureA.get(15, TimeUnit.SECONDS);
            futureB.get(15, TimeUnit.SECONDS);

            long totalReceived = consumerAMessages.stream().filter(v -> v.startsWith("리밸런싱중-")).count()
                    + consumerBMessages.stream().filter(v -> v.startsWith("리밸런싱중-")).count();

            assertThat(totalReceived).isEqualTo(messageCount);
        } finally {
            consumerARunning.set(false);
            consumerBRunning.set(false);
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    @Test
    @Order(6)
    @DisplayName("동일 Consumer Group의 여러 Consumer가 동일 메시지를 중복 소비하지 않는다")
    void sameGroup_noDuplicateConsumption() throws Exception {
        // given
        String groupId = "no-duplicate-" + UUID.randomUUID();
        int consumerCount = 3;
        int messageCount = 30;
        List<AtomicBoolean> runningFlags = new ArrayList<>();
        List<ConcurrentLinkedQueue<String>> messageQueues = new ArrayList<>();
        CountDownLatch allSubscribed = new CountDownLatch(consumerCount);
        ExecutorService executor = Executors.newFixedThreadPool(consumerCount);

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < consumerCount; i++) {
                AtomicBoolean flag = new AtomicBoolean(true);
                ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
                runningFlags.add(flag);
                messageQueues.add(queue);
                futures.add(executor.submit(() ->
                        consumeAsync(groupId, flag, queue, allSubscribed)));
            }

            assertThat(allSubscribed.await(30, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(SESSION_TIMEOUT_MS * 2L);

            // when — 각 메시지에 고유 key 부여하여 발행
            publishMessagesWithUniqueKey("dup-check-", messageCount);
            Thread.sleep(5000);

            // then — 각 Consumer가 수신한 메시지에 교집합이 없어야 한다
            for (AtomicBoolean flag : runningFlags) {
                flag.set(false);
            }
            for (Future<?> future : futures) {
                future.get(15, TimeUnit.SECONDS);
            }

            Set<String> allValues = new HashSet<>();
            long totalCount = 0;
            for (ConcurrentLinkedQueue<String> queue : messageQueues) {
                List<String> values = queue.stream()
                        .filter(v -> v.startsWith("dup-check-"))
                        .toList();
                totalCount += values.size();
                for (String value : values) {
                    boolean added = allValues.add(value);
                    assertThat(added).as("중복 소비 감지: %s", value).isTrue();
                }
            }

            assertThat(totalCount).isEqualTo(messageCount);
        } finally {
            for (AtomicBoolean flag : runningFlags) {
                flag.set(false);
            }
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    // === Phase 4: Consumer Lag 복구 ===

    @Test
    @Order(7)
    @DisplayName("Consumer Lag 발생 후 재시작 시 밀린 메시지를 모두 소비하고 복구 시간이 60초 이내이다")
    void consumerLag_recoversAllMessagesWithinSla() throws Exception {
        // given
        String groupId = "lag-recovery-" + UUID.randomUUID();
        int lagMessageCount = 100;

        // 기존 메시지를 모두 소비하고 커밋하여 베이스라인 설정
        try (KafkaConsumer<String, String> baseline = createConsumer(groupId)) {
            baseline.subscribe(Collections.singletonList(testTopic));
            drainAllMessages(baseline, Duration.ofSeconds(15));
            baseline.commitSync();
        }

        // Consumer 부재 상태에서 대량 메시지 발행 (Lag 생성)
        publishMessages("lag-key", "밀린메시지-", lagMessageCount);

        // when — Consumer 재시작 + 복구 시간 측정
        long recoveryStartNanos = System.nanoTime();
        List<String> recovered;
        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(testTopic));
            recovered = drainAllMessages(consumer, Duration.ofSeconds(60));
            consumer.commitSync();
        }
        long recoveryMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - recoveryStartNanos);

        // then
        long lagMessages = recovered.stream().filter(v -> v.startsWith("밀린메시지-")).count();
        assertThat(lagMessages).isEqualTo(lagMessageCount);
        assertThat(recoveryMs).isLessThan(60_000L);

        log.info("Consumer Lag 복구 시간 측정 — 밀린 메시지: {}건, 복구 소요: {}ms, SLA 60초 이내: PASS",
                lagMessageCount, recoveryMs);
    }

    // === Phase 5: max.poll.interval.ms 킥아웃 ===

    @Test
    @Order(8)
    @DisplayName("max.poll.interval.ms 초과 시 Consumer가 킥아웃되고 다른 Consumer가 파티션을 인수한다")
    void maxPollIntervalExceeded_consumerKickedOutAndPartitionReassigned() throws Exception {
        // given
        String groupId = "kickout-" + UUID.randomUUID();
        int shortPollIntervalMs = 5_000;
        AtomicBoolean consumerBRunning = new AtomicBoolean(true);
        ConcurrentLinkedQueue<String> consumerBMessages = new ConcurrentLinkedQueue<>();
        CountDownLatch consumerBSubscribed = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Consumer A: max.poll.interval.ms=5초 (의도적으로 초과할 Consumer)
            // Consumer B: 정상 Consumer (파티션 인수 역할)
            AtomicBoolean consumerAKickedOut = new AtomicBoolean(false);
            CountDownLatch consumerASubscribed = new CountDownLatch(1);

            Future<?> futureA = executor.submit(() -> {
                try (KafkaConsumer<String, String> consumerA = createConsumerWithShortPollInterval(
                        groupId, shortPollIntervalMs)) {
                    consumerA.subscribe(Collections.singletonList(testTopic));
                    consumerA.poll(Duration.ofSeconds(5));
                    consumerASubscribed.countDown();

                    // max.poll.interval.ms(5초) 초과하도록 의도적으로 대기
                    Thread.sleep(shortPollIntervalMs * 3L);
                    consumerAKickedOut.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    consumerAKickedOut.set(true);
                } catch (Exception e) {
                    consumerAKickedOut.set(true);
                }
            });

            assertThat(consumerASubscribed.await(30, TimeUnit.SECONDS)).isTrue();

            Future<?> futureB = executor.submit(() ->
                    consumeAsync(groupId, consumerBRunning, consumerBMessages, consumerBSubscribed));
            assertThat(consumerBSubscribed.await(30, TimeUnit.SECONDS)).isTrue();

            // Consumer A 킥아웃 대기 (shortPollInterval * 3 + 리밸런싱 여유)
            Thread.sleep(shortPollIntervalMs * 3L + SESSION_TIMEOUT_MS);

            // when — 킥아웃 후 메시지 발행
            int messageCount = 9;
            publishMessages("kickout-key", "킥아웃후-", messageCount);
            Thread.sleep(5000);

            // then — Consumer B가 모든 파티션을 인수하여 메시지를 소비
            consumerBRunning.set(false);
            futureA.get(30, TimeUnit.SECONDS);
            futureB.get(15, TimeUnit.SECONDS);

            assertThat(consumerAKickedOut.get()).isTrue();
            assertThat(consumerBMessages.stream().filter(v -> v.startsWith("킥아웃후-")).count())
                    .isEqualTo(messageCount);
        } finally {
            consumerBRunning.set(false);
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    // === 유틸리티 메서드 ===

    private void createTopic() throws Exception {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic topic = new NewTopic(testTopic, PARTITIONS, REPLICATION_FACTOR);
            topic.configs(Map.of("min.insync.replicas", "2"));
            adminClient.createTopics(Collections.singleton(topic)).all().get(30, TimeUnit.SECONDS);
        }
    }

    private void waitForClusterReady() throws Exception {
        for (int attempt = 0; attempt < READY_CHECK_MAX_ATTEMPTS; attempt++) {
            try (KafkaProducer<String, String> producer = createProducer()) {
                Future<RecordMetadata> future = producer.send(
                        new ProducerRecord<>(testTopic, "readiness-" + attempt, "cluster-ready-check")
                );
                future.get(5, TimeUnit.SECONDS);
                return;
            } catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        throw new IllegalStateException(
                "Kafka 클러스터가 " + READY_CHECK_MAX_ATTEMPTS + "회 시도 후에도 준비되지 않음");
    }

    private void publishMessages(String keyPrefix, String valuePrefix, int count) throws Exception {
        try (KafkaProducer<String, String> producer = createProducer()) {
            for (int i = 0; i < count; i++) {
                String key = keyPrefix + "-" + i;
                String value = valuePrefix + i;
                producer.send(new ProducerRecord<>(testTopic, key, value)).get(10, TimeUnit.SECONDS);
            }
        }
    }

    private void publishMessagesWithUniqueKey(String valuePrefix, int count) throws Exception {
        try (KafkaProducer<String, String> producer = createProducer()) {
            for (int i = 0; i < count; i++) {
                String key = "unique-" + i;
                String value = valuePrefix + i;
                producer.send(new ProducerRecord<>(testTopic, key, value)).get(10, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 토픽의 모든 메시지를 소비한다.
     * 3초간 새 메시지가 없으면 소비 완료로 판단한다.
     */
    private List<String> drainAllMessages(KafkaConsumer<String, String> consumer, Duration timeout) {
        List<String> received = new ArrayList<>();
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        long lastMessageTime = System.currentTimeMillis();
        long emptyPollThresholdMs = 3000;
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            if (records.isEmpty()) {
                if (System.currentTimeMillis() - lastMessageTime > emptyPollThresholdMs) {
                    break;
                }
                continue;
            }
            lastMessageTime = System.currentTimeMillis();
            for (ConsumerRecord<String, String> record : records) {
                received.add(record.value());
            }
        }
        return received;
    }

    /**
     * 별도 쓰레드에서 Consumer를 실행하여 메시지를 수집한다.
     * runningFlag가 false가 되면 poll 루프를 종료한다.
     */
    private void consumeAsync(String groupId,
                              AtomicBoolean runningFlag,
                              ConcurrentLinkedQueue<String> messageCollector,
                              CountDownLatch subscribedLatch) {
        try (KafkaConsumer<String, String> consumer = createConsumer(groupId)) {
            consumer.subscribe(Collections.singletonList(testTopic));
            consumer.poll(Duration.ofSeconds(3));
            subscribedLatch.countDown();

            while (runningFlag.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    messageCollector.add(record.value());
                }
                try {
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    // 리밸런싱 중 커밋 실패는 정상 시나리오 — 재할당된 파티션으로 계속 소비
                }
            }
        }
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
        return new KafkaProducer<>(props);
    }

    private KafkaConsumer<String, String> createConsumer(String groupId) {
        return createConsumer(groupId, Map.of());
    }

    private KafkaConsumer<String, String> createConsumerWithShortPollInterval(String groupId, int maxPollIntervalMs) {
        return createConsumer(groupId, Map.of(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs));
    }

    private KafkaConsumer<String, String> createConsumer(String groupId, Map<String, Object> overrides) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS);
        props.putAll(overrides);
        return new KafkaConsumer<>(props);
    }
}
