package com.personal.marketnote.fulfillment.adapter.out.scheduler;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.fulfillment.domain.FasstoAccessToken;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFasstoAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FasstoWarehousingInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoWarehousingResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFasstoWarehousingUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFasstoAuthUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFasstoAllStockUseCase;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFasstoWarehousingPollingCommand;
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
class FasstoWarehousingPollingSchedulerTest {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Seoul");

    private RecordingTaskScheduler taskScheduler;
    private MutableClock clock;

    @Mock
    private RequestFasstoAuthUseCase requestFasstoAuthUseCase;
    @Mock
    private GetFasstoWarehousingUseCase getFasstoWarehousingUseCase;
    @Mock
    private SyncFasstoAllStockUseCase syncFasstoAllStockUseCase;

    private FasstoWarehousingPollingScheduler scheduler;

    @BeforeEach
    void setUp() {
        taskScheduler = new RecordingTaskScheduler();
        clock = new MutableClock(parseInstant("2026-02-08T08:00:00"));
        scheduler = new FasstoWarehousingPollingScheduler(
                taskScheduler,
                clock,
                requestFasstoAuthUseCase,
                getFasstoWarehousingUseCase,
                syncFasstoAllStockUseCase
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
    @DisplayName("wrkStat 4가 발견되면 재고 동기화를 호출한다")
    void poll_whenConfirmed_syncsStocks() {
        clock.setInstant(parseInstant("2026-02-08T10:00:00"));
        when(requestFasstoAuthUseCase.requestAccessToken())
                .thenReturn(FasstoAccessToken.of("token", "20260116120000"));

        FasstoWarehousingInfoResult item = buildWarehousingInfo("ORD-1", "WH1", "4");
        GetFasstoWarehousingResult result = GetFasstoWarehousingResult.of(1, List.of(item));
        when(getFasstoWarehousingUseCase.getWarehousing(
                argThat(command -> FormatValidator.hasValue(command) && "4".equals(command.wrkStat()))
        ))
                .thenReturn(result);
        scheduler.schedule(buildCommand("ORD-1"));
        taskScheduler.tasks().get(0).runnable().run();

        ArgumentCaptor<SyncFasstoAllStockCommand> syncCaptor =
                ArgumentCaptor.forClass(SyncFasstoAllStockCommand.class);
        verify(syncFasstoAllStockUseCase).syncAll(syncCaptor.capture());
        assertThat(syncCaptor.getValue().customerCode()).isEqualTo("CUST");
        assertThat(syncCaptor.getValue().whCd()).isEqualTo("WH1");

        ArgumentCaptor<GetFasstoWarehousingCommand> commandCaptor =
                ArgumentCaptor.forClass(GetFasstoWarehousingCommand.class);
        verify(getFasstoWarehousingUseCase).getWarehousing(commandCaptor.capture());
        assertThat(commandCaptor.getValue().startDate()).isEqualTo("20260116");
        assertThat(commandCaptor.getValue().endDate()).isEqualTo("20260116");
        assertThat(commandCaptor.getValue().wrkStat()).isEqualTo("4");

        verify(getFasstoWarehousingUseCase, never())
                .getWarehousing(argThat(command -> "5".equals(command.wrkStat())));
    }

    @Test
    @DisplayName("wrkStat 4가 없고 5가 있으면 5 기준으로 동기화를 수행한다")
    void poll_whenCompleted_syncsStocks() {
        clock.setInstant(parseInstant("2026-02-08T11:00:00"));
        when(requestFasstoAuthUseCase.requestAccessToken())
                .thenReturn(FasstoAccessToken.of("token", "20260116120000"));

        FasstoWarehousingInfoResult completed = buildWarehousingInfo("ORD-3", "WH2", "5");

        when(getFasstoWarehousingUseCase.getWarehousing(any()))
                .thenAnswer(invocation -> {
                    GetFasstoWarehousingCommand command = invocation.getArgument(0);
                    if ("4".equals(command.wrkStat())) {
                        return GetFasstoWarehousingResult.of(0, List.of());
                    }
                    return GetFasstoWarehousingResult.of(1, List.of(completed));
                });

        scheduler.schedule(buildCommand("ORD-3"));
        taskScheduler.tasks().get(0).runnable().run();

        ArgumentCaptor<SyncFasstoAllStockCommand> syncCaptor =
                ArgumentCaptor.forClass(SyncFasstoAllStockCommand.class);
        verify(syncFasstoAllStockUseCase).syncAll(syncCaptor.capture());
        assertThat(syncCaptor.getValue().whCd()).isEqualTo("WH2");

        ArgumentCaptor<GetFasstoWarehousingCommand> commandCaptor =
                ArgumentCaptor.forClass(GetFasstoWarehousingCommand.class);
        verify(getFasstoWarehousingUseCase, org.mockito.Mockito.times(2))
                .getWarehousing(commandCaptor.capture());
        assertThat(commandCaptor.getAllValues())
                .extracting(GetFasstoWarehousingCommand::wrkStat)
                .containsExactly("4", "5");
    }

    private ScheduleFasstoWarehousingPollingCommand buildCommand(String ordNo) {
        return ScheduleFasstoWarehousingPollingCommand.of("CUST", ordNo, "20260116");
    }

    private FasstoWarehousingInfoResult buildWarehousingInfo(String ordNo, String whCd, String wrkStat) {
        return FasstoWarehousingInfoResult.of(
                "20260116",
                whCd,
                null,
                ordNo,
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
                wrkStat,
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
