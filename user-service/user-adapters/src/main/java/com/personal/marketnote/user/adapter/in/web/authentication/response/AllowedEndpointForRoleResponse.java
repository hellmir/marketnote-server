package com.personal.marketnote.user.adapter.in.web.authentication.response;

import java.util.List;

public record AllowedEndpointForRoleResponse(String endpoint, List<RoleResponse> roles) {
}
