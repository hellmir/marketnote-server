package com.personal.marketnote.community.adapter.out.event.exception;

public class CommunityEventSerializationException extends RuntimeException {
    public CommunityEventSerializationException(String topic, Throwable cause) {
        super("ERR_COMMUNITY_EVENT_01::커뮤니티 이벤트 직렬화에 실패했습니다. topic=" + topic, cause);
    }
}
