package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.Clock;
import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record EventEnvelope<T>(
        String eventId,
        String eventType,
        String source,
        LocalDateTime timestamp,
        T payload
) {

    public static <T> EventEnvelope<T> of(String eventType, String source, T payload, Clock clock) {
        return EventEnvelope.<T>builder()
                .eventId(UuidCreator.getTimeOrdered().toString())
                .eventType(eventType)
                .source(source)
                .timestamp(LocalDateTime.now(clock))
                .payload(payload)
                .build();
    }

    @SuppressWarnings("unchecked")
    public <R> R getPayloadAs(Class<R> type, ObjectMapper objectMapper) {
        if (type.isInstance(payload)) {
            return type.cast(payload);
        }
        return objectMapper.convertValue(payload, type);
    }
}
