package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.result.CheckNicknameResult;

/**
 * 닉네임 중복 여부 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 닉네임 중복 여부를 조회합니다.
 */
public interface CheckNicknameUseCase {
    /**
     * @param nickname 닉네임
     * @return 닉네임 중복 여부 {@link CheckNicknameResult}
     * @Date 2026-03-19
     * @Author 성효빈
     * @Description 닉네임 중복 여부를 조회합니다.
     */
    CheckNicknameResult checkNickname(String nickname);
}
