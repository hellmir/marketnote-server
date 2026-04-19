package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

/**
 * 풀필먼트 인증 해제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 풀필먼트 인증 해제 기능을 제공합니다.
 */
public interface DisconnectFulfillmentAuthUseCase {
    /**
     * @param accessToken Fulfillment access token
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description Disconnect Fulfillment access token.
     */
    void disconnectAccessToken(String accessToken);
}
