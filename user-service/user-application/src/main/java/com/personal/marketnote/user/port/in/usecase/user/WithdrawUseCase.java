package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.result.WithdrawResult;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;

import java.util.Map;

/**
 * 회원 탈퇴 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-29
 * @Description 회원 탈퇴 기능을 제공합니다.
 */
public interface WithdrawUseCase {
    /**
     * @param id                회원 ID
     * @param vendorCredentials 벤더별 연결 해제에 필요한 자격 증명 (예: 구글 액세스 토큰)
     * @Date 2025-12-29
     * @Author 성효빈
     * @Description 회원에서 탈퇴하고 소셜 연결 해제 결과를 반환합니다.
     */
    WithdrawResult withdrawUser(Long id, Map<AuthVendor, String> vendorCredentials);
}
