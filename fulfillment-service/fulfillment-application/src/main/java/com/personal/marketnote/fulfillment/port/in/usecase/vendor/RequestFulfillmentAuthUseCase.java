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
}
