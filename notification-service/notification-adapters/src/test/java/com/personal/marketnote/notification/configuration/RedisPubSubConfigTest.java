package com.personal.marketnote.notification.configuration;

import com.personal.marketnote.notification.adapter.in.web.sse.subscriber.SseEventRedisSubscriber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RedisPubSubConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private SseEventRedisSubscriber sseEventRedisSubscriber;

    @Test
    @DisplayName("SSE 알림 이벤트 채널 토픽이 정상 생성된다")
    void shouldCreateSseNotificationEventsTopic() {
        // given
        RedisPubSubConfig config = new RedisPubSubConfig();

        // when
        ChannelTopic topic = config.sseNotificationEventsTopic();

        // then
        assertThat(topic.getTopic()).isEqualTo("sse:notification-events");
    }

    @Test
    @DisplayName("RedisMessageListenerContainer 빈이 구독자와 토픽으로 정상 구성된다")
    void shouldCreateRedisMessageListenerContainer() {
        // given
        RedisPubSubConfig config = new RedisPubSubConfig();
        ChannelTopic topic = config.sseNotificationEventsTopic();

        // when
        RedisMessageListenerContainer container = config.sseRedisMessageListenerContainer(
                redisConnectionFactory, sseEventRedisSubscriber, topic
        );

        // then
        assertThat(container).isNotNull();
        assertThat(container.getConnectionFactory()).isEqualTo(redisConnectionFactory);
    }
}
