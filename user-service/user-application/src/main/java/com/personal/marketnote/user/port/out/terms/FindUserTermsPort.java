package com.personal.marketnote.user.port.out.terms;

import com.personal.marketnote.user.domain.user.Terms;

import java.util.List;

/**
 * 회원 이용 약관 조회 포트
 *
 * @Author 성효빈
 * @Date 2025-12-27
 * @Description 회원 이용 약관 조회 기능을 제공합니다.
 */
public interface FindUserTermsPort {
    /**
     * @return 전체 이용 약관 목록 {@link List<Terms>}
     * @Date 2025-12-27
     * @Author 성효빈
     * @Description 전체 이용 약관 목록을 조회합니다.
     */
    List<Terms> findAll();
}
