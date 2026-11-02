package com.personal.marketnote.user.port.out.user;

import com.personal.marketnote.user.domain.user.UserFingerprint;

public interface SaveUserFingerprintPort {
    void save(UserFingerprint userFingerprint);
}
