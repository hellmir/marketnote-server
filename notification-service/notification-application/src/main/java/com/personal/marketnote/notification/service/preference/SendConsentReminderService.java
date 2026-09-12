package com.personal.marketnote.notification.service.preference;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.port.in.command.SendNotificationCommand;
import com.personal.marketnote.notification.port.in.usecase.notification.SendNotificationUseCase;
import com.personal.marketnote.notification.port.in.usecase.preference.SendConsentReminderUseCase;
import com.personal.marketnote.notification.port.out.preference.FindNotificationPreferencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SendConsentReminderService implements SendConsentReminderUseCase {

    private static final int CONSENT_REMINDER_YEARS = 2;
    private static final String CONSENT_REMINDER_TEMPLATE_CODE = "CONSENT_REMINDER";
    private static final String DELIVERY_CHANNEL = "PUSH_AND_IN_APP";

    private final FindNotificationPreferencePort findNotificationPreferencePort;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final Clock clock;

    @Override
    public int sendConsentReminders() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusYears(CONSENT_REMINDER_YEARS);
        List<NotificationPreference> duePreferences = findNotificationPreferencePort.findConsentReminderDue(threshold);

        if (duePreferences.isEmpty()) {
            return 0;
        }

        int sentCount = 0;
        for (NotificationPreference preference : duePreferences) {
            try {
                SendNotificationCommand command = new SendNotificationCommand(
                        preference.getUserId(),
                        CONSENT_REMINDER_TEMPLATE_CODE,
                        Map.of(),
                        DELIVERY_CHANNEL,
                        null
                );
                sendNotificationUseCase.sendNotification(command);
                sentCount++;
            } catch (Exception e) {
                log.error("수신 동의 재고지 발송 실패: userId={}", preference.getUserId(), e);
            }
        }

        log.info("수신 동의 재고지 발송 완료: 대상={}명, 성공={}건", duePreferences.size(), sentCount);
        return sentCount;
    }
}
