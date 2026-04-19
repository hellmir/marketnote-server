package com.personal.marketnote.fulfillment.port.out.vendor;

/**
 * 풀필먼트 인증 해제 포트
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 풀필먼트 인증 해제 기능을 제공합니다.
 */
public interface DisconnectFulfillmentAuthPort {

    /**
     * @param accessToken 해제할 풀필먼트 액세스 토큰
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description 풀필먼트 액세스 토큰을 해제합니다.
     */
    void disconnectAccessToken(String accessToken);
}
