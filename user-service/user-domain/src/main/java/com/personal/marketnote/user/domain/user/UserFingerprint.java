package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.exception.FingerprintNoValueException;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserFingerprint {
    private Long id;
    private Long userId;
    private String fingerprint;
    private FingerprintSource source;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;

    public static UserFingerprint from(UserFingerprintCreateState state) {
        if (FormatValidator.hasNoValue(state.getFingerprint())) {
            throw new FingerprintNoValueException("fingerprint는 필수값입니다.");
        }

        LocalDateTime now = state.getFirstSeenAt();
        return UserFingerprint.builder()
                .userId(state.getUserId())
                .fingerprint(state.getFingerprint())
                .source(state.getSource())
                .firstSeenAt(now)
                .lastSeenAt(now)
                .build();
    }

    public static UserFingerprint from(UserFingerprintSnapshotState state) {
        return UserFingerprint.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .fingerprint(state.getFingerprint())
                .source(state.getSource())
                .firstSeenAt(state.getFirstSeenAt())
                .lastSeenAt(state.getLastSeenAt())
                .build();
    }

    public void updateLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
