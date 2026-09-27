package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.DltTopicRegistry;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@ConditionalOnProperty(
        prefix = "spring.kafka",
        name = "bootstrap-servers",
        matchIfMissing = false
)
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final short REPLICATION_FACTOR = 3;
    private static final String MIN_INSYNC_REPLICAS_KEY = "min.insync.replicas";
    private static final String MIN_INSYNC_REPLICAS_VALUE = "2";

    @Bean
    public KafkaAdmin.NewTopics topics() {
        List<NewTopic> all = buildAllTopics();
        return new KafkaAdmin.NewTopics(all.toArray(new NewTopic[0]));
    }

    List<NewTopic> buildAllTopics() {
        Set<String> topicNames = new LinkedHashSet<>();
        topicNames.addAll(KafkaTopicConstants.getAllTopics());
        topicNames.addAll(DltTopicRegistry.getAllDltTopics());

        List<NewTopic> topics = new ArrayList<>(topicNames.size());
        for (String name : topicNames) {
            topics.add(buildTopic(name));
        }
        return topics;
    }

    private NewTopic buildTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICATION_FACTOR)
                .config(MIN_INSYNC_REPLICAS_KEY, MIN_INSYNC_REPLICAS_VALUE)
                .build();
    }
}
