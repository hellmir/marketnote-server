package com.personal.marketnote.user.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UserFingerprintCreateState {
    private final Long userId;
    private final String fingerprint;
    private final FingerprintSource source;
    private final LocalDateTime firstSeenAt;
}
