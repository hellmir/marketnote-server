package com.personal.marketnote.user.port.in.event;

import com.personal.marketnote.user.domain.user.FingerprintSource;

public record UserFingerprintSavedEvent(
        Long userId,
        String fingerprint,
        FingerprintSource source
) {
}
