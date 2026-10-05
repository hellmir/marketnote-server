package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.preference.SendConsentReminderUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsentReminderSchedulerTest {

    @InjectMocks
    private ConsentReminderScheduler consentReminderScheduler;

    @Mock
    private SendConsentReminderUseCase sendConsentReminderUseCase;

    @Test
    @DisplayName("수신 동의 재알림 스케줄러가 UseCase를 호출하여 재알림을 발송한다")
    void shouldSendConsentReminders() {
        // given
        when(sendConsentReminderUseCase.sendConsentReminders()).thenReturn(3);

        // when
        consentReminderScheduler.sendConsentReminders();

        // then
        verify(sendConsentReminderUseCase).sendConsentReminders();
    }

    @Test
    @DisplayName("발송 건수가 0이면 UseCase 호출은 정상 수행되고 로그만 다르다")
    void shouldHandleZeroSentCount() {
        // given
        when(sendConsentReminderUseCase.sendConsentReminders()).thenReturn(0);

        // when
        consentReminderScheduler.sendConsentReminders();

        // then
        verify(sendConsentReminderUseCase).sendConsentReminders();
    }
}
