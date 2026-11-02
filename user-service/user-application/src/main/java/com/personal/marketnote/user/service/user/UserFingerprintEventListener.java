package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.UserFingerprint;
import com.personal.marketnote.user.domain.user.UserFingerprintCreateState;
import com.personal.marketnote.user.port.in.event.UserFingerprintSavedEvent;
import com.personal.marketnote.user.port.out.user.SaveUserFingerprintPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFingerprintEventListener {
    private final SaveUserFingerprintPort saveUserFingerprintPort;
    private final Clock clock;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void handleUserFingerprintSavedEvent(UserFingerprintSavedEvent event) {
        if (FormatValidator.hasNoValue(event.fingerprint())) {
            return;
        }

        try {
            UserFingerprint userFingerprint = UserFingerprint.from(
                    UserFingerprintCreateState.builder()
                            .userId(event.userId())
                            .fingerprint(event.fingerprint())
                            .source(event.source())
                            .firstSeenAt(LocalDateTime.now(clock))
                            .build()
            );

            saveUserFingerprintPort.save(userFingerprint);
        } catch (Exception e) {
            String masked = event.fingerprint().substring(0, Math.min(8, event.fingerprint().length())) + "***";
            log.warn("디바이스 fingerprint 저장 실패. userId={}, fingerprintPrefix={}", event.userId(), masked, e);
        }
    }
}
