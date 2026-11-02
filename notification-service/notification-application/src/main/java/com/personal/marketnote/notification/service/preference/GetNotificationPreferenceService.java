package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.port.in.result.preference.GetNotificationPreferenceResult;
import com.personal.marketnote.notification.port.in.usecase.preference.GetNotificationPreferenceUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetNotificationPreferenceService implements GetNotificationPreferenceUseCase {

    private final FindNotificationPreferencePort findNotificationPreferencePort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public List<GetNotificationPreferenceResult> getNotificationPreferences(Long userId) {
        return findNotificationPreferencePort.findAllByUserId(userId).stream()
                .map(preference -> new GetNotificationPreferenceResult(
                        preference.getNotificationType().name(),
                        preference.getNotificationType().getDescription(),
                        preference.isEnabled(),
                        preference.getConsentedAt()
                ))
                .toList();
    }
}
