package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.User;

/**
 * 회원 저장 포트
 *
 * @Author 성효빈
 * @Date 2025-12-27
 * @Description 회원 저장 기능을 제공합니다.
 */
public interface SaveUserPort {
    /**
     * @param user 회원
     * @return 저장된 회원 {@link User}
     * @Date 2025-12-27
     * @Author 성효빈
     * @Description 회원을 저장합니다.
     */
    User save(User user);
}
