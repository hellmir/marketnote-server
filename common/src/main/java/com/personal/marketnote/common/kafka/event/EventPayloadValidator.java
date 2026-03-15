package com.personal.marketnote.common.kafka.event;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
public final class EventPayloadValidator {

    private EventPayloadValidator() {
    }

    public static boolean hasInvalidEnvelope(EventEnvelope<?> envelope, ConsumerRecord<?, ?> record) {
        if (FormatValidator.hasNoValue(envelope)) {
            log.warn("이벤트 envelope이 null. topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            return true;
        }
        return false;
    }

    public static boolean hasEventTypeMismatch(EventEnvelope<?> envelope, String expectedEventType) {
        if (FormatValidator.hasNoValue(envelope.eventType()) || !envelope.eventType().equals(expectedEventType)) {
            log.warn("이벤트 타입 불일치. eventId={}, expected={}, actual={}",
                    envelope.eventId(), expectedEventType, envelope.eventType());
            return true;
        }
        return false;
    }

    public static boolean hasInvalidIds(String eventId, IdField... fields) {
        for (IdField field : fields) {
            if (FormatValidator.hasNoValue(field.value()) || field.value() <= 0) {
                log.error("유효하지 않은 이벤트 페이로드. eventId={}, {}={}",
                        eventId, field.name(), field.value());
                return true;
            }
        }
        return false;
    }

    public record IdField(String name, Long value) {
    }

    public static IdField id(String name, Long value) {
        return new IdField(name, value);
    }
}
