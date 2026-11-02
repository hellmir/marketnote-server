package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;

/**
 * 풀필먼트 인증 요청 포트
 *
 * @Author 성효빈
 * @Date 2026-01-24
 * @Description 풀필먼트 인증 요청 기능을 제공합니다.
 */
public interface RequestFulfillmentAuthPort {

    /**
     * @return 풀필먼트 액세스 토큰 {@link FulfillmentAccessToken}
     * @Date 2026-01-24
     * @Author 성효빈
     * @Description 풀필먼트 액세스 토큰을 요청합니다.
     */
    FulfillmentAccessToken requestAccessToken();
}
