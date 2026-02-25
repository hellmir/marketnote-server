package com.personal.marketnote.user.port.in.usecase.user;

import com.personal.marketnote.user.port.in.command.UpdateUserInfoCommand;

/**
 * 회원 정보 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2025-12-28
 * @Description 회원 정보 수정 기능을 제공합니다.
 */
public interface UpdateUserUseCase {
    /**
     * @param isAdmin               관리자 여부
     * @param id                    회원 ID
     * @param updateUserInfoCommand 회원 정보 수정 요청 커맨드
     * @Author 성효빈
     * @Date 2025-12-28
     * @Description 회원 정보를 수정합니다.
     */
    void updateUserInfo(boolean isAdmin, Long id, UpdateUserInfoCommand updateUserInfoCommand);
}
