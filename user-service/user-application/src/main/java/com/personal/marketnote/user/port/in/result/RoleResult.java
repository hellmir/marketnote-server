package com.personal.marketnote.user.port.in.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.marketnote.user.domain.authentication.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RoleResult {
    private final String id;
    private final String name;

    @JsonIgnore
    private final String code;

    public static RoleResult of(Role role) {
        return new RoleResult(role.getId(), role.getName(), role.getCode());
    }
}

