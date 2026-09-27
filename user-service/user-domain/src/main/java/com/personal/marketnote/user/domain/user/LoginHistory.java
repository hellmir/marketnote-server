package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.ipaddress.IpAddress;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class LoginHistory {
    private Long id;
    private User user;
    private AuthVendor authVendor;
    private IpAddress ipAddress;
    private LocalDateTime createdAt;

    public static LoginHistory of(User user, AuthVendor authVendor, String ipAddress) {
        return LoginHistory.builder()
                .user(user)
                .authVendor(authVendor)
                .ipAddress(IpAddress.of(ipAddress))
                .build();
    }

    public static LoginHistory from(LoginHistorySnapshotState state) {
        return LoginHistory.builder()
                .id(state.getId())
                .user(state.getUser())
                .authVendor(state.getAuthVendor())
                .ipAddress(state.getIpAddress())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
