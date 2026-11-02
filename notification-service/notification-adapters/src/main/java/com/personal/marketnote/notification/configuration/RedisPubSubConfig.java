package com.personal.marketnote.notification.configuration;

import com.personal.marketnote.notification.adapter.in.web.sse.subscriber.SseEventRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    public static final String SSE_NOTIFICATION_EVENTS_CHANNEL = "sse:notification-events";

    @Bean
    public ChannelTopic sseNotificationEventsTopic() {
        return new ChannelTopic(SSE_NOTIFICATION_EVENTS_CHANNEL);
    }

    @Bean
    public RedisMessageListenerContainer sseRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            SseEventRedisSubscriber sseEventRedisSubscriber,
            ChannelTopic sseNotificationEventsTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(sseEventRedisSubscriber, sseNotificationEventsTopic);
        return container;
    }
}
