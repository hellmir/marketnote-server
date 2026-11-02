package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.User;

public record VerifyCodeResult(
        Long id,
        String roleId
) {
    public static VerifyCodeResult from(User user) {
        return new VerifyCodeResult(user.getId(), user.getRole().getId());
    }
}
