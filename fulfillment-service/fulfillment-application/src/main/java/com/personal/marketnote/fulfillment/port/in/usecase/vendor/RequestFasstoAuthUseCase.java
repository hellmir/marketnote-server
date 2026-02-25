package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;

/**
 * 파스토 인증 요청 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-24
 * @Description 파스토 인증 요청 기능을 제공합니다.
 */
public interface RequestFasstoAuthUseCase {
    /**
     * @return Fassto access token {@link FasstoAccessToken}
     * @Date 2026-01-24
     * @Author 성효빈
     * @Description Request a Fassto access token.
     */
    FasstoAccessToken requestAccessToken();
}
