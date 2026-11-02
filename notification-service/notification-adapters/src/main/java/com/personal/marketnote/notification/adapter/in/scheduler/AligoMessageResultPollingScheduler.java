package com.personal.marketnote.notification.adapter.in.scheduler;

import com.personal.marketnote.notification.port.in.usecase.aligo.PollAligoMessageDetailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "aligo.polling", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AligoMessageResultPollingScheduler {

    private final PollAligoMessageDetailUseCase pollAligoMessageDetailUseCase;

    @Scheduled(fixedDelayString = "${aligo.polling.fixed-delay-ms:300000}")
    public void pollMessageResults() {
        log.debug("알리고 메시지 결과 폴링 스케줄러 실행");
        int processedCount = pollAligoMessageDetailUseCase.poll();
        if (processedCount > 0) {
            log.info("알리고 메시지 결과 폴링 스케줄러 완료: {}건 처리", processedCount);
        }
    }
}
