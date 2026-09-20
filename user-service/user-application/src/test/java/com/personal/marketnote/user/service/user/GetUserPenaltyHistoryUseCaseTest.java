package com.personal.marketnote.user.service.user;

import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySnapshotState;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySortProperty;
import com.personal.marketnote.user.port.in.result.GetUserPenaltyHistoryResult;
import com.personal.marketnote.user.port.out.user.FindUserPenaltyHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserPenaltyHistoryUseCaseTest {
    @Mock
    private FindUserPenaltyHistoryPort findUserPenaltyHistoryPort;

    @InjectMocks
    private GetUserPenaltyHistoryService getUserPenaltyHistoryService;

    @Test
    @DisplayName("패널티 내역 조회 시 페이징과 정렬 조건이 적용되어 결과가 매핑된다")
    void shouldMapResultsAndApplyPageable() {
        // given
        Long userId = 1L;
        int pageSize = 10;
        int pageNumber = 0;
        Sort.Direction sortDirection = Sort.Direction.DESC;
        UserPenaltyHistorySortProperty sortProperty = UserPenaltyHistorySortProperty.ID;

        UserPenaltyHistory history1 = buildHistory(10L, userId, 0, 1, "게시글 도배 행위", 99L,
                LocalDateTime.of(2026, 4, 13, 10, 0));
        UserPenaltyHistory history2 = buildHistory(11L, userId, 1, 2, "약관 위반", 99L,
                LocalDateTime.of(2026, 4, 13, 11, 0));

        Page<UserPenaltyHistory> histories = new PageImpl<>(
                List.of(history1, history2),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue())),
                2
        );

        when(findUserPenaltyHistoryPort.findUserPenaltyHistoriesByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(histories);

        // when
        Page<GetUserPenaltyHistoryResult> result = getUserPenaltyHistoryService.getUserPenaltyHistories(
                userId, pageSize, pageNumber, sortDirection, sortProperty
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        GetUserPenaltyHistoryResult first = result.getContent().get(0);
        assertThat(first.id()).isEqualTo(10L);
        assertThat(first.userId()).isEqualTo(userId);
        assertThat(first.previousCount()).isEqualTo(0);
        assertThat(first.currentCount()).isEqualTo(1);
        assertThat(first.reason()).isEqualTo("게시글 도배 행위");
        assertThat(first.createdBy()).isEqualTo(99L);
        assertThat(first.createdAt()).isEqualTo(LocalDateTime.of(2026, 4, 13, 10, 0));

        GetUserPenaltyHistoryResult second = result.getContent().get(1);
        assertThat(second.id()).isEqualTo(11L);
        assertThat(second.previousCount()).isEqualTo(1);
        assertThat(second.currentCount()).isEqualTo(2);
        assertThat(second.reason()).isEqualTo("약관 위반");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(findUserPenaltyHistoryPort).findUserPenaltyHistoriesByUserId(pageableCaptor.capture(), eq(userId));

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);
        Sort.Order order = pageable.getSort().getOrderFor(sortProperty.getLowerValue());
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(sortDirection);

        verifyNoMoreInteractions(findUserPenaltyHistoryPort);
    }

    @Test
    @DisplayName("패널티 내역이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyPageWhenNoHistories() {
        // given
        Long userId = 2L;
        int pageSize = 5;
        int pageNumber = 1;
        Sort.Direction sortDirection = Sort.Direction.ASC;
        UserPenaltyHistorySortProperty sortProperty = UserPenaltyHistorySortProperty.ID;

        Page<UserPenaltyHistory> histories = new PageImpl<>(
                List.of(),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue())),
                0
        );

        when(findUserPenaltyHistoryPort.findUserPenaltyHistoriesByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(histories);

        // when
        Page<GetUserPenaltyHistoryResult> result = getUserPenaltyHistoryService.getUserPenaltyHistories(
                userId, pageSize, pageNumber, sortDirection, sortProperty
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(findUserPenaltyHistoryPort).findUserPenaltyHistoriesByUserId(any(Pageable.class), eq(userId));
        verifyNoMoreInteractions(findUserPenaltyHistoryPort);
    }

    private UserPenaltyHistory buildHistory(
            Long id,
            Long userId,
            int previousCount,
            int currentCount,
            String reason,
            Long createdBy,
            LocalDateTime createdAt
    ) {
        return UserPenaltyHistory.from(
                UserPenaltyHistorySnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .previousCount(previousCount)
                        .currentCount(currentCount)
                        .reason(reason)
                        .createdBy(createdBy)
                        .createdAt(createdAt)
                        .build()
        );
    }
}
