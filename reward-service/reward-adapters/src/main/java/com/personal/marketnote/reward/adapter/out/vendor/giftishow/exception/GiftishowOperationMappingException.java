package com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception;

/**
 * Giftishow operation → targetType 매핑이 빈 초기화 시점에 누락되어 있을 때 발생한다.
 *
 * <p>{@code @PostConstruct} 검증 실패용 startup-only 예외로, 운영 경로에서는 throw되지 않는다.
 * 빈 초기화 자체를 차단하여 운영 시점 매핑 누락을 사전에 방지한다.</p>
 */
public class GiftishowOperationMappingException extends RuntimeException {

    public GiftishowOperationMappingException(String message) {
        super(message);
    }
}
