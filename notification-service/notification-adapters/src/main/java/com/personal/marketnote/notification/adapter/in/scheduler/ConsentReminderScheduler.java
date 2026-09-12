package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.preference.SendConsentReminderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.consent-reminder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConsentReminderScheduler {

    private final SendConsentReminderUseCase sendConsentReminderUseCase;

    @Scheduled(cron = "${notification.consent-reminder.cron:0 0 9 * * *}")
    public void sendConsentReminders() {
        log.debug("수신 동의 재고지 스케줄러 실행");
        int sentCount = sendConsentReminderUseCase.sendConsentReminders();
        if (sentCount > 0) {
            log.info("수신 동의 재고지 스케줄러 완료: {}건 발송", sentCount);
        }
    }
}
