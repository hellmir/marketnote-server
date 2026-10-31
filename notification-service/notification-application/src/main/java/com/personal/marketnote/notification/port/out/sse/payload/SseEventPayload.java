package com.personal.marketnote.notification.port.out.sse.payload;

/**
 * SSE 이벤트 wire payload의 sealed 마커. permits 대상만 PublishSseEventPort로 전달 가능하다.
 */
public sealed interface SseEventPayload permits UnreadCountChangedPayload {
}
