package com.personal.marketnote.fulfillment.adapter.out.scheduler;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentWarehousingPollingSchedulerTest {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private RecordingTaskScheduler taskScheduler;
    private MutableClock clock;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    @Mock
    private GetFulfillmentWarehousingUseCase getFulfillmentWarehousingUseCase;
    @Mock
    private SyncFulfillmentAllStockUseCase syncFulfillmentAllStockUseCase;

    private FulfillmentWarehousingPollingScheduler scheduler;

    @BeforeEach
    void setUp() {
        taskScheduler = new RecordingTaskScheduler();
        clock = new MutableClock(parseInstant("2026-02-08T08:00:00"));
        scheduler = new FulfillmentWarehousingPollingScheduler(
                taskScheduler,
                clock,
                requestFulfillmentAuthUseCase,
                getFulfillmentWarehousingUseCase,
                syncFulfillmentAllStockUseCase
        );
    }

    @Test
    @DisplayName("09:30 이전 요청이면 당일 09:30부터 polling을 시작한다")
    void schedule_beforeWindow_startsAtWindowStart() {
        clock.setInstant(parseInstant("2026-02-08T07:30:00"));

        scheduler.schedule(buildCommand("ORD-1"));

        assertThat(taskScheduler.tasks()).hasSize(1);
        assertThat(taskScheduler.tasks().get(0).scheduledAt())
                .isEqualTo(parseInstant("2026-02-08T09:30:00"));
    }

    @Test
    @DisplayName("19:00 이후 요청이면 스케줄링하지 않는다")
    void schedule_afterWindow_doesNotSchedule() {
        clock.setInstant(parseInstant("2026-02-08T19:30:00"));

        scheduler.schedule(buildCommand("ORD-2"));

        assertThat(taskScheduler.tasks()).isEmpty();
    }

    @Test
    @DisplayName("workStatus 4가 발견되면 재고 동기화를 호출한다")
    void poll_whenConfirmed_syncsStocks() {
        clock.setInstant(parseInstant("2026-02-08T10:00:00"));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token", "20260116120000"));

        FulfillmentWarehousingInfoResult item = buildWarehousingInfo("ORD-1", "WH1", "4");
        GetFulfillmentWarehousingResult result = GetFulfillmentWarehousingResult.of(1, List.of(item));
        when(getFulfillmentWarehousingUseCase.getWarehousing(
                argThat(command -> FormatValidator.hasValue(command) && "4".equals(command.workStatus()))
        ))
                .thenReturn(result);
        scheduler.schedule(buildCommand("ORD-1"));
        taskScheduler.tasks().get(0).runnable().run();

        ArgumentCaptor<SyncFulfillmentAllStockCommand> syncCaptor =
                ArgumentCaptor.forClass(SyncFulfillmentAllStockCommand.class);
        verify(syncFulfillmentAllStockUseCase).syncAll(syncCaptor.capture());
        assertThat(syncCaptor.getValue().customerCode()).isEqualTo("CUST");
        assertThat(syncCaptor.getValue().whCd()).isEqualTo("WH1");

        ArgumentCaptor<GetFulfillmentWarehousingCommand> commandCaptor =
                ArgumentCaptor.forClass(GetFulfillmentWarehousingCommand.class);
        verify(getFulfillmentWarehousingUseCase).getWarehousing(commandCaptor.capture());
        assertThat(commandCaptor.getValue().startDate()).isEqualTo("20260116");
        assertThat(commandCaptor.getValue().endDate()).isEqualTo("20260116");
        assertThat(commandCaptor.getValue().workStatus()).isEqualTo("4");

        verify(getFulfillmentWarehousingUseCase, never())
                .getWarehousing(argThat(command -> "5".equals(command.workStatus())));
    }

    @Test
    @DisplayName("workStatus 4가 없고 5가 있으면 5 기준으로 동기화를 수행한다")
    void poll_whenCompleted_syncsStocks() {
        clock.setInstant(parseInstant("2026-02-08T11:00:00"));
        when(requestFulfillmentAuthUseCase.requestAccessToken())
                .thenReturn(FulfillmentAccessToken.of("token", "20260116120000"));

        FulfillmentWarehousingInfoResult completed = buildWarehousingInfo("ORD-3", "WH2", "5");

        when(getFulfillmentWarehousingUseCase.getWarehousing(any()))
                .thenAnswer(invocation -> {
                    GetFulfillmentWarehousingCommand command = invocation.getArgument(0);
                    if ("4".equals(command.workStatus())) {
                        return GetFulfillmentWarehousingResult.of(0, List.of());
                    }
                    return GetFulfillmentWarehousingResult.of(1, List.of(completed));
                });

        scheduler.schedule(buildCommand("ORD-3"));
        taskScheduler.tasks().get(0).runnable().run();

        ArgumentCaptor<SyncFulfillmentAllStockCommand> syncCaptor =
                ArgumentCaptor.forClass(SyncFulfillmentAllStockCommand.class);
        verify(syncFulfillmentAllStockUseCase).syncAll(syncCaptor.capture());
        assertThat(syncCaptor.getValue().whCd()).isEqualTo("WH2");

        ArgumentCaptor<GetFulfillmentWarehousingCommand> commandCaptor =
                ArgumentCaptor.forClass(GetFulfillmentWarehousingCommand.class);
        verify(getFulfillmentWarehousingUseCase, org.mockito.Mockito.times(2))
                .getWarehousing(commandCaptor.capture());
        assertThat(commandCaptor.getAllValues())
                .extracting(GetFulfillmentWarehousingCommand::workStatus)
                .containsExactly("4", "5");
    }

    private ScheduleFulfillmentWarehousingPollingCommand buildCommand(String orderNumber) {
        return ScheduleFulfillmentWarehousingPollingCommand.of("CUST", orderNumber, "20260116");
    }

    private FulfillmentWarehousingInfoResult buildWarehousingInfo(String orderNumber, String warehouseCode, String workStatus) {
        return FulfillmentWarehousingInfoResult.of(
                "20260116",
                warehouseCode,
                null,
                orderNumber,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                workStatus,
                null,
                null,
                null,
                List.of()
        );
    }

    private Instant parseInstant(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(DEFAULT_ZONE).toInstant();
    }

    private static class RecordingTaskScheduler implements TaskScheduler {
        private final List<ScheduledTask> tasks = new ArrayList<>();

        List<ScheduledTask> tasks() {
            return tasks;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            ScheduledTask scheduledTask = new ScheduledTask(task, startTime, new TestScheduledFuture());
            tasks.add(scheduledTask);
            return scheduledTask.future();
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            throw new UnsupportedOperationException("Trigger scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, java.util.Date startTime) {
            throw new UnsupportedOperationException("Date scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, java.util.Date startTime, long period) {
            throw new UnsupportedOperationException("Fixed rate scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
            throw new UnsupportedOperationException("Fixed rate scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            throw new UnsupportedOperationException("Fixed rate scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
            throw new UnsupportedOperationException("Fixed rate scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, java.util.Date startTime, long delay) {
            throw new UnsupportedOperationException("Fixed delay scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
            throw new UnsupportedOperationException("Fixed delay scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
            throw new UnsupportedOperationException("Fixed delay scheduling is not supported in this test.");
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
            throw new UnsupportedOperationException("Fixed delay scheduling is not supported in this test.");
        }
    }

    private record ScheduledTask(Runnable runnable, Instant scheduledAt, TestScheduledFuture future) {
    }

    private static class TestScheduledFuture implements ScheduledFuture<Object> {
        private boolean cancelled;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return DEFAULT_ZONE;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
