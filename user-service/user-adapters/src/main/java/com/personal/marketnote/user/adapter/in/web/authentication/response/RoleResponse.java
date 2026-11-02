package com.personal.marketnote.user.adapter.in.web.authentication.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.marketnote.user.port.in.result.RoleResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RoleResponse {
    private final String id;
    private final String name;

    @JsonIgnore
    private final String code;

    public static RoleResponse of(RoleResult roleResult) {
        return new RoleResponse(roleResult.getId(), roleResult.getName(), roleResult.getCode());
    }
}
