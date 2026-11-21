package com.personal.marketnote.community.adapter.out.event.exception;

import com.personal.marketnote.common.outbox.exception.BaseEventSerializationException;

public class CommunityEventSerializationException extends BaseEventSerializationException {

    public CommunityEventSerializationException(String topic, String partitionKey, String eventId, Throwable cause) {
        super("ERR_COMMUNITY_EVENT_01", "커뮤니티 이벤트", topic, partitionKey, eventId, cause);
    }
}
