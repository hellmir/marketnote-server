package com.personal.marketnote.reward.service.point;

import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.domain.point.UserPointHistorySnapshotState;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.InvalidPointHistoryDateRangeException;
import com.personal.marketnote.reward.port.in.command.point.GetUserPointHistoryCommand;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;
import com.personal.marketnote.reward.port.out.point.FindUserPointHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserPointHistoryUseCaseTest {

    @InjectMocks
    private GetUserPointHistoryService getUserPointHistoryService;

    @Mock
    private FindUserPointHistoryPort findUserPointHistoryPort;

    private static final Long USER_ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 5, 10, 0);
    private static final LocalDate DEFAULT_START_DATE = GetUserPointHistoryService.DEFAULT_START_DATE;
    private static final LocalDate DEFAULT_END_DATE = GetUserPointHistoryService.DEFAULT_END_DATE;

    private UserPointHistory createHistory(Long id, Long amount, UserPointSourceType sourceType, LocalDateTime accumulatedAt) {
        return UserPointHistory.from(UserPointHistorySnapshotState.builder()
                .id(id)
                .userId(USER_ID)
                .amount(amount)
                .isReflected(Boolean.TRUE)
                .sourceType(sourceType)
                .sourceId(100L)
                .reason("테스트")
                .accumulatedAt(accumulatedAt)
                .createdAt(accumulatedAt)
                .build());
    }

    private GetUserPointHistoryCommand createCommand(UserPointHistoryFilter filter) {
        return GetUserPointHistoryCommand.builder()
                .userId(USER_ID)
                .filter(filter)
                .build();
    }

    private GetUserPointHistoryCommand createCommand(UserPointHistoryFilter filter, LocalDate startDate, LocalDate endDate) {
        return GetUserPointHistoryCommand.builder()
                .userId(USER_ID)
                .filter(filter)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Nested
    @DisplayName("필터 지정 조회")
    class FilterSpecifiedTest {

        @Test
        @DisplayName("전체 필터로 포인트 이력을 조회하면 모든 이력이 반환된다")
        void shouldReturnAllHistoriesWithAllFilter() {
            // given
            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, NOW),
                    createHistory(2L, -200L, UserPointSourceType.ORDER, NOW)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            assertThat(result.histories().getFirst().count()).isEqualTo(2);
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE);
        }

        @Test
        @DisplayName("적립 필터로 포인트 이력을 조회한다")
        void shouldReturnHistoriesWithAccrualFilter() {
            // given
            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, NOW)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ACCRUAL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ACCRUAL)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            assertThat(result.histories().getFirst().count()).isEqualTo(1);
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ACCRUAL, DEFAULT_START_DATE, DEFAULT_END_DATE);
        }

        @Test
        @DisplayName("차감 필터로 포인트 이력을 조회한다")
        void shouldReturnHistoriesWithDeductionFilter() {
            // given
            List<UserPointHistory> histories = List.of(
                    createHistory(1L, -300L, UserPointSourceType.ORDER, NOW)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.DEDUCTION, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.DEDUCTION)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.DEDUCTION, DEFAULT_START_DATE, DEFAULT_END_DATE);
        }
    }

    @Nested
    @DisplayName("필터 미지정 조회")
    class FilterNullTest {

        @Test
        @DisplayName("필터가 null이면 전체 필터로 조회한다")
        void shouldUseAllFilterWhenFilterIsNull() {
            // given
            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, NOW)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(null)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE);
        }
    }

    @Nested
    @DisplayName("빈 결과")
    class EmptyResultTest {

        @Test
        @DisplayName("이력이 없으면 빈 결과를 반환한다")
        void shouldReturnEmptyResultWhenNoHistories() {
            // given
            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(Collections.emptyList());

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL)
            );

            // then
            assertThat(result.histories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("날짜별 그룹핑")
    class DateGroupingTest {

        @Test
        @DisplayName("같은 날짜의 이력은 하나의 그룹으로 묶인다")
        void shouldGroupHistoriesBySameDate() {
            // given
            LocalDateTime morning = LocalDateTime.of(2026, 3, 5, 9, 0);
            LocalDateTime afternoon = LocalDateTime.of(2026, 3, 5, 15, 0);

            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, morning),
                    createHistory(2L, 300L, UserPointSourceType.ATTENDENCE, afternoon)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            assertThat(result.histories().getFirst().count()).isEqualTo(2);
        }

        @Test
        @DisplayName("다른 날짜의 이력은 별도 그룹으로 분리된다")
        void shouldSeparateHistoriesByDifferentDate() {
            // given
            LocalDateTime day1 = LocalDateTime.of(2026, 3, 4, 10, 0);
            LocalDateTime day2 = LocalDateTime.of(2026, 3, 5, 10, 0);

            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, day1),
                    createHistory(2L, 300L, UserPointSourceType.ATTENDENCE, day2)
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, DEFAULT_END_DATE))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL)
            );

            // then
            assertThat(result.histories()).hasSize(2);
            assertThat(result.histories().getFirst().count()).isEqualTo(1);
            assertThat(result.histories().get(1).count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("기간 필터 조회")
    class DateRangeFilterTest {

        @Test
        @DisplayName("시작일과 종료일을 지정하면 해당 기간이 Port에 전달된다")
        void shouldPassDateRangeToPort() {
            // given
            LocalDate startDate = LocalDate.of(2026, 3, 1);
            LocalDate endDate = LocalDate.of(2026, 3, 31);

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, startDate, endDate))
                    .thenReturn(Collections.emptyList());

            // when
            getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL, startDate, endDate)
            );

            // then
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ALL, startDate, endDate);
        }

        @Test
        @DisplayName("시작일만 지정하면 종료일은 기본값으로 설정된다")
        void shouldUseDefaultEndDateWhenOnlyStartDateProvided() {
            // given
            LocalDate startDate = LocalDate.of(2026, 3, 1);

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, startDate, DEFAULT_END_DATE))
                    .thenReturn(Collections.emptyList());

            // when
            getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL, startDate, null)
            );

            // then
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ALL, startDate, DEFAULT_END_DATE);
        }

        @Test
        @DisplayName("종료일만 지정하면 시작일은 기본값으로 설정된다")
        void shouldUseDefaultStartDateWhenOnlyEndDateProvided() {
            // given
            LocalDate endDate = LocalDate.of(2026, 3, 31);

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, endDate))
                    .thenReturn(Collections.emptyList());

            // when
            getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL, null, endDate)
            );

            // then
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ALL, DEFAULT_START_DATE, endDate);
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            // given
            LocalDate startDate = LocalDate.of(2026, 3, 31);
            LocalDate endDate = LocalDate.of(2026, 3, 1);

            // when & then
            assertThatThrownBy(() -> getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ALL, startDate, endDate)
            )).isInstanceOf(InvalidPointHistoryDateRangeException.class);

            verifyNoInteractions(findUserPointHistoryPort);
        }

        @Test
        @DisplayName("기간 필터와 타입 필터를 함께 적용한다")
        void shouldApplyDateRangeWithTypeFilter() {
            // given
            LocalDate startDate = LocalDate.of(2026, 3, 1);
            LocalDate endDate = LocalDate.of(2026, 3, 31);

            List<UserPointHistory> histories = List.of(
                    createHistory(1L, 500L, UserPointSourceType.ORDER, LocalDateTime.of(2026, 3, 15, 10, 0))
            );

            when(findUserPointHistoryPort.findByUserId(USER_ID, UserPointHistoryFilter.ACCRUAL, startDate, endDate))
                    .thenReturn(histories);

            // when
            GetUserPointHistoryResult result = getUserPointHistoryService.getUserPointHistories(
                    createCommand(UserPointHistoryFilter.ACCRUAL, startDate, endDate)
            );

            // then
            assertThat(result.histories()).hasSize(1);
            verify(findUserPointHistoryPort).findByUserId(USER_ID, UserPointHistoryFilter.ACCRUAL, startDate, endDate);
        }
    }
}
