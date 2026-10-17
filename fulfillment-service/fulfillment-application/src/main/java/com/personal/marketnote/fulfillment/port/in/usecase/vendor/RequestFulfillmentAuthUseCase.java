package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;

/**
 * 풀필먼트 인증 요청 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-24
 * @Description 풀필먼트 인증 요청 기능을 제공합니다.
 */
public interface RequestFulfillmentAuthUseCase {
    /**
     * @return Fulfillment access token {@link FulfillmentAccessToken}
     * @Date 2026-01-24
     * @Author 성효빈
     * @Description Request a Fulfillment access token.
     */
    FulfillmentAccessToken requestAccessToken();

    /**
     * @Date 2026-10-17
     * @Author 성효빈
     * @Description 캐시된 토큰을 무효화한다. INVALID_ACCESS 등 외부 차단 신호를 받았을 때 호출한다.
     */
    void invalidateCache();
}
