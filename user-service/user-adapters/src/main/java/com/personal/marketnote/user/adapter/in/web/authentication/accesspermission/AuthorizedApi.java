package com.personal.marketnote.user.adapter.in.web.authentication.accesspermission;

import com.personal.marketnote.user.constant.AuthorizedHttpMethod;

import java.util.Set;

public record AuthorizedApi(
        String endpoint,
        AuthorizedHttpMethod httpMethod,
        Set<String> roleIds
) {
}
