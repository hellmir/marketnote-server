package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.ipaddress.IpAddress;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHistorySnapshotState {
    private final Long id;
    private final User user;
    private final AuthVendor authVendor;
    private final IpAddress ipAddress;
    private final LocalDateTime createdAt;
}

