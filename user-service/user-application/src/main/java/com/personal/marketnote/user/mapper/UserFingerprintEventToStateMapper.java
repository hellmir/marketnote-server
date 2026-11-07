package com.personal.marketnote.user.mapper;

import com.personal.marketnote.user.domain.user.UserFingerprintCreateState;
import com.personal.marketnote.user.port.in.event.UserFingerprintSavedEvent;

import java.time.LocalDateTime;

public class UserFingerprintEventToStateMapper {
    private UserFingerprintEventToStateMapper() {
    }

    public static UserFingerprintCreateState mapToCreateState(UserFingerprintSavedEvent event, LocalDateTime firstSeenAt) {
        return UserFingerprintCreateState.builder()
                .userId(event.userId())
                .fingerprint(event.fingerprint())
                .source(event.source())
                .firstSeenAt(firstSeenAt)
                .build();
    }
}
