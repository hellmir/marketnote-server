package com.personal.marketnote.user.port.in.usecase.terms;

import com.personal.marketnote.user.port.in.result.GetTermsResult;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;

/**
 * 이용 약관 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 이용 약관 조회 기능을 제공합니다.
 */
public interface GetTermsUseCase {
    /**
     * @return 전체 이용 약관 목록 {@link GetTermsResult}
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 전체 이용 약관 목록을 조회합니다.
     */
    GetTermsResult getAllTerms();

    /**
     * @param id 회원 ID
     * @return GetUserTermsResult 회원 이용 약관 동의 여부 목록
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 회원 이용 약관 동의 여부 목록을 조회합니다.
     */
    GetUserTermsResult getUserTerms(Long id);
}
