package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class RoleNotFoundException extends DomainNotFoundException {
    private static final String MESSAGE = "역할을 찾을 수 없습니다.";

    private final String roleId;

    public RoleNotFoundException(String roleId) {
        super(MESSAGE);
        this.roleId = roleId;
    }
}
