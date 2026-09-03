package com.personal.marketnote.notification.adapter.in.web.sse.registry;

public class SseConnectionLimitExceededException extends RuntimeException {

    public SseConnectionLimitExceededException(int maxConnections) {
        super("ERR_SSE_01::SSE 최대 커넥션 수를 초과했습니다. maxConnections=" + maxConnections);
    }
}
