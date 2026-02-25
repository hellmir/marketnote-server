package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;

/**
 * 파스토 인증 요청 포트
 *
 * @Author 성효빈
 * @Date 2026-01-24
 * @Description 파스토 인증 요청 기능을 제공합니다.
 */
public interface RequestFasstoAuthPort {

    /**
     * @return 파스토 액세스 토큰 {@link FasstoAccessToken}
     * @Date 2026-01-24
     * @Author 성효빈
     * @Description 파스토 액세스 토큰을 요청합니다.
     */
    FasstoAccessToken requestAccessToken();
}
