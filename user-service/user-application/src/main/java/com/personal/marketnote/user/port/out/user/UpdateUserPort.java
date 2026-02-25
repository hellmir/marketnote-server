package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;

/**
 * 회원 수정 포트
 *
 * @Author 성효빈
 * @Date 2025-12-27
 * @Description 회원 수정 기능을 제공합니다.
 */
public interface UpdateUserPort {
    /**
     * @param user 회원
     * @throws UserNotFoundException 회원을 찾을 수 없는 경우
     * @Date 2025-12-27
     * @Author 성효빈
     * @Description 회원 정보를 수정합니다.
     */
    void update(User user) throws UserNotFoundException;
}
