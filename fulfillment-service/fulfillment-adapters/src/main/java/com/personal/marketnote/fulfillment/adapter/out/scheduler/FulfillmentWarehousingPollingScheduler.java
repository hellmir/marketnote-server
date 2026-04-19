package com.personal.marketnote.fulfillment.adapter.out.scheduler;

import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentWarehousingInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFulfillmentAllStockUseCase;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFulfillmentWarehousingPollingCommand;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFulfillmentWarehousingPollingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class FulfillmentWarehousingPollingScheduler implements ScheduleFulfillmentWarehousingPollingPort {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime POLLING_START_TIME = LocalTime.of(9, 30);
    private static final LocalTime POLLING_END_TIME = LocalTime.of(19, 0);
    private static final int POLLING_INTERVAL_MINUTES = 30;
    private static final String COMPLETED_WORK_STATUS_CONFIRMED = "4";
    private static final String COMPLETED_WORK_STATUS_COMPLETED = "5";

    private final TaskScheduler taskScheduler;
    private final Clock clock;
    private final RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    private final GetFulfillmentWarehousingUseCase getFulfillmentWarehousingUseCase;
    private final SyncFulfillmentAllStockUseCase syncFulfillmentAllStockUseCase;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public void schedule(ScheduleFulfillmentWarehousingPollingCommand command) {
        Instant requestAt = clock.instant();
        if (FormatValidator.hasNoValue(command)
                || FormatValidator.hasNoValue(command.customerCode())
                || FormatValidator.hasNoValue(command.orderNumber())) {
            return;
        }

        PollingContext context = PollingContext.from(command, requestAt);
        if (FormatValidator.hasNoValue(context.startDate())
                || FormatValidator.hasNoValue(context.endDate())) {
            log.warn("Warehousing polling skipped due to invalid date: ordNo={}", context.ordNo());
            return;
        }

        stopPolling(context.ordNo());
        Instant firstRun = resolveFirstRunAt(context, requestAt);
        if (FormatValidator.hasValue(firstRun)) {
            scheduleNext(context, firstRun);
        }
    }

    private void scheduleNext(PollingContext context, Instant scheduledAt) {
        if (FormatValidator.hasNoValue(scheduledAt) || scheduledAt.isAfter(context.windowEnd())) {
            stopPolling(context.ordNo());
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> poll(context),
                scheduledAt
        );
        if (FormatValidator.hasValue(future)) {
            scheduledTasks.put(context.ordNo(), future);
        }
    }

    private void poll(PollingContext context) {
        Instant now = clock.instant();
        if (context.isExpired(now)) {
            stopPolling(context.ordNo());
            return;
        }
        if (!context.isWithinWindow(now)) {
            scheduleNext(context, context.windowStart());
            return;
        }

        try {
            FulfillmentAccessToken accessToken = requestFulfillmentAuthUseCase.requestAccessToken();
            if (FormatValidator.hasNoValue(accessToken) || FormatValidator.hasNoValue(accessToken.getValue())) {
                scheduleNext(context, resolveNextRunAt(context, now));
                return;
            }

            FulfillmentWarehousingInfoResult completed = resolveCompletedWarehousing(
                    accessToken.getValue(),
                    context,
                    COMPLETED_WORK_STATUS_CONFIRMED
            );
            if (FormatValidator.hasNoValue(completed)) {
                completed = resolveCompletedWarehousing(
                        accessToken.getValue(),
                        context,
                        COMPLETED_WORK_STATUS_COMPLETED
                );
            }
            if (FormatValidator.hasValue(completed)) {
                String warehouseCode = completed.warehouseCode();
                if (FormatValidator.hasValue(warehouseCode)) {
                    syncFulfillmentAllStockUseCase.syncAll(
                            SyncFulfillmentAllStockCommand.of(context.customerCode(), warehouseCode)
                    );
                    stopPolling(context.ordNo());
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to poll Fulfillment warehousing: ordNo={}", context.ordNo(), e);
        }

        scheduleNext(context, resolveNextRunAt(context, now));
    }

    private FulfillmentWarehousingInfoResult resolveCompletedWarehousing(
            String accessToken,
            PollingContext context,
            String workStatus
    ) {
        GetFulfillmentWarehousingResult result = getFulfillmentWarehousingUseCase.getWarehousing(
                GetFulfillmentWarehousingCommand.of(
                        context.customerCode(),
                        accessToken,
                        context.startDate(),
                        context.endDate(),
                        null,
                        context.ordNo(),
                        workStatus
                )
        );
        if (FormatValidator.hasNoValue(result) || FormatValidator.hasNoValue(result.warehousing())) {
            return null;
        }

        return result.warehousing().stream()
                .filter(item -> FormatValidator.hasValue(item) && workStatus.equals(item.workStatus()))
                .findFirst()
                .orElse(null);
    }

    private void stopPolling(String ordNo) {
        ScheduledFuture<?> scheduled = scheduledTasks.remove(ordNo);
        if (FormatValidator.hasValue(scheduled)) {
            scheduled.cancel(false);
        }
    }

    private Instant resolveFirstRunAt(PollingContext context, Instant requestAt) {
        if (requestAt.isAfter(context.windowEnd())) {
            return null;
        }

        if (requestAt.isBefore(context.windowStart())) {
            return context.windowStart();
        }

        return requestAt;
    }

    private Instant resolveNextRunAt(PollingContext context, Instant now) {
        Instant next = ZonedDateTime.ofInstant(now, DEFAULT_ZONE)
                .plusMinutes(POLLING_INTERVAL_MINUTES)
                .toInstant();
        if (next.isAfter(context.windowEnd())) {
            return null;
        }
        return next;
    }

    private static String normalizeOrdDate(Instant requestAt) {
        LocalDate requestDate = resolveRequestDate(requestAt);
        return requestDate.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    private static LocalDate resolveRequestDate(Instant requestAt) {
        LocalDate fallbackDate = LocalDate.now(DEFAULT_ZONE);
        if (FormatValidator.hasNoValue(requestAt)) {
            return fallbackDate;
        }

        return ZonedDateTime.ofInstant(requestAt, DEFAULT_ZONE).toLocalDate();
    }

    private record PollingContext(
            String customerCode,
            String ordNo,
            String startDate,
            String endDate,
            LocalDate requestDate,
            Instant windowStart,
            Instant windowEnd
    ) {
        private static PollingContext from(ScheduleFulfillmentWarehousingPollingCommand command, Instant requestAt) {
            LocalDate requestDate = resolveRequestDate(requestAt);
            String normalizedDate = normalizeOrdDate(requestAt);
            Instant windowStart = ZonedDateTime.of(requestDate, POLLING_START_TIME, DEFAULT_ZONE).toInstant();
            Instant windowEnd = ZonedDateTime.of(requestDate, POLLING_END_TIME, DEFAULT_ZONE).toInstant();
            return new PollingContext(
                    command.customerCode(),
                    command.orderNumber(),
                    normalizedDate,
                    normalizedDate,
                    requestDate,
                    windowStart,
                    windowEnd
            );
        }

        private boolean isWithinWindow(Instant now) {
            return !now.isBefore(windowStart) && !now.isAfter(windowEnd);
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(windowEnd);
        }
    }
}
