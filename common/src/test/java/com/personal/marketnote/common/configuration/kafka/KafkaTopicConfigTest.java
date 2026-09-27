package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.DltTopicRegistry;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KafkaTopicConfig 테스트")
class KafkaTopicConfigTest {

    private static final int EXPECTED_PARTITIONS = 3;
    private static final short EXPECTED_REPLICATION_FACTOR = 3;
    private static final String EXPECTED_MIN_INSYNC_REPLICAS = "2";
    private static final String MIN_INSYNC_REPLICAS_KEY = "min.insync.replicas";

    private final KafkaTopicConfig kafkaTopicConfig = new KafkaTopicConfig();

    @Test
    @DisplayName("KafkaAdmin.NewTopics 빈이 정상 등록된다")
    void shouldRegisterNewTopicsBean() {
        KafkaAdmin.NewTopics newTopics = kafkaTopicConfig.topics();

        assertThat(newTopics).isNotNull();
        assertThat(kafkaTopicConfig.buildAllTopics()).isNotEmpty();
    }

    @Test
    @DisplayName("KafkaTopicConstants의 모든 토픽 상수가 NewTopic으로 등록된다")
    void shouldRegisterAllConstantsAsNewTopics() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();
        Set<String> registeredNames = registered.stream()
                .map(NewTopic::name)
                .collect(Collectors.toSet());

        assertThat(registeredNames).containsAll(KafkaTopicConstants.getAllTopics());
    }

    @Test
    @DisplayName("DltTopicRegistry의 모든 DLT 토픽이 NewTopic으로 등록된다")
    void shouldRegisterAllDltTopicsAsNewTopics() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();
        Set<String> registeredNames = registered.stream()
                .map(NewTopic::name)
                .collect(Collectors.toSet());

        assertThat(registeredNames).containsAll(DltTopicRegistry.getAllDltTopics());
    }

    @Test
    @DisplayName("일반 토픽과 DLT 토픽 수의 합이 NewTopic 수와 일치한다 (누락 방지)")
    void shouldNotMissAnyTopic() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        int expectedSize = KafkaTopicConstants.getAllTopics().size() + DltTopicRegistry.getAllDltTopics().size();

        assertThat(registered).hasSize(expectedSize);
    }

    @Test
    @DisplayName("등록된 NewTopic의 이름이 중복되지 않는다")
    void shouldNotHaveDuplicateTopicNames() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        long distinctCount = registered.stream()
                .map(NewTopic::name)
                .distinct()
                .count();

        assertThat(distinctCount).isEqualTo(registered.size());
    }

    @Test
    @DisplayName("DLT_SUFFIX 자체는 토픽 이름으로 등록되지 않는다")
    void shouldNotRegisterDltSuffixAsTopicName() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        assertThat(registered)
                .extracting(NewTopic::name)
                .doesNotContain(KafkaTopicConstants.DLT_SUFFIX);
    }

    @Test
    @DisplayName("모든 NewTopic의 partitions가 3이다")
    void shouldHavePartitionsThree() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        assertThat(registered)
                .allSatisfy(topic -> assertThat(topic.numPartitions()).isEqualTo(EXPECTED_PARTITIONS));
    }

    @Test
    @DisplayName("모든 NewTopic의 replicationFactor가 3이다")
    void shouldHaveReplicationFactorThree() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        assertThat(registered)
                .allSatisfy(topic -> assertThat(topic.replicationFactor()).isEqualTo(EXPECTED_REPLICATION_FACTOR));
    }

    @Test
    @DisplayName("모든 NewTopic의 min.insync.replicas config가 2이다")
    void shouldHaveMinInSyncReplicasTwo() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();

        assertThat(registered).allSatisfy(topic -> {
            Map<String, String> configs = topic.configs();
            assertThat(configs).containsEntry(MIN_INSYNC_REPLICAS_KEY, EXPECTED_MIN_INSYNC_REPLICAS);
        });
    }

    @Test
    @DisplayName("DLT 토픽도 일반 토픽과 동일한 partitions/RF/min.insync.replicas를 가진다")
    void shouldDltTopicsHaveSameSettings() {
        List<NewTopic> registered = kafkaTopicConfig.buildAllTopics();
        Set<String> dltTopicNames = Set.copyOf(DltTopicRegistry.getAllDltTopics());

        List<NewTopic> dltTopics = registered.stream()
                .filter(topic -> dltTopicNames.contains(topic.name()))
                .toList();

        assertThat(dltTopics).hasSize(dltTopicNames.size());
        assertThat(dltTopics).allSatisfy(topic -> {
            assertThat(topic.numPartitions()).isEqualTo(EXPECTED_PARTITIONS);
            assertThat(topic.replicationFactor()).isEqualTo(EXPECTED_REPLICATION_FACTOR);
            assertThat(topic.configs()).containsEntry(MIN_INSYNC_REPLICAS_KEY, EXPECTED_MIN_INSYNC_REPLICAS);
        });
    }
}
