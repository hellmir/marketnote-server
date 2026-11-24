package com.personal.marketnote.user.port.in.usecase.user;

/**
 * 추천 회원 초대 코드 입력 건너뛰기 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-11-22
 * @Description 회원 가입 직후 초대 코드 입력 화면에서 건너뛰기를 명시적으로 선택한 경우를 처리합니다.
 */
public interface SkipReferredUserCodeInputUseCase {
    /**
     * @param requestUserId 요청 회원 ID
     * @Date 2026-11-22
     * @Author 성효빈
     * @Description 추천 회원 초대 코드 입력 절차를 건너뛰기로 표시합니다.
     */
    void skipReferredUserCodeInput(Long requestUserId);
}
