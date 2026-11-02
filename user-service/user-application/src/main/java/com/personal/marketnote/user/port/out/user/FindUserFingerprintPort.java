package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.UserFingerprint;

import java.util.List;
import java.util.Optional;

public interface FindUserFingerprintPort {
    Optional<UserFingerprint> findByUserIdAndFingerprint(Long userId, String fingerprint);

    List<UserFingerprint> findByUserId(Long userId);
}
