package com.personal.marketnote.fulfillment.adapter.in.scheduler;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.in.command.PollShippingStatusCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.PollShippingStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fulfillment.delivery-polling", name = "enabled", havingValue = "true")
public class FulfillmentDeliveryStatusPollingScheduler {

    private static final ZoneId POLLING_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime POLLING_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime POLLING_END_TIME = LocalTime.of(21, 0);

    private final PollShippingStatusUseCase pollShippingStatusUseCase;
    private final FulfillmentAuthProperties fasstoAuthProperties;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${fulfillment.delivery-polling.interval-ms:1800000}")
    public void pollDeliveryStatuses() {
        if (!isWithinPollingWindow()) {
            log.debug("폴링 시간대(08~21시)가 아닙니다. 스킵합니다.");
            return;
        }

        String customerCode = fasstoAuthProperties.getCustomerCode();
        if (FormatValidator.hasNoValue(customerCode)) {
            log.warn("Fulfillment customer code가 설정되지 않았습니다. 배송 상태 폴링을 스킵합니다.");
            return;
        }

        try {
            pollShippingStatusUseCase.pollShippingStatuses(new PollShippingStatusCommand(customerCode));
        } catch (Exception e) {
            log.error("배송 상태 폴링 실패: customerCode={}", customerCode, e);
        }
    }

    private boolean isWithinPollingWindow() {
        LocalTime now = LocalTime.now(clock.withZone(POLLING_ZONE));
        return !now.isBefore(POLLING_START_TIME) && now.isBefore(POLLING_END_TIME);
    }
}
