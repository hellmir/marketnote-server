package com.personal.marketnote.user.service.user;

import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.domain.user.UserStatusHistorySortProperty;
import com.personal.marketnote.user.domain.user.UserStatusHistorySnapshotState;
import com.personal.marketnote.user.port.in.result.GetUserStatusHistoryResult;
import com.personal.marketnote.user.port.out.user.FindUserStatusHistoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserStatusHistoryUseCaseTest {
    @InjectMocks
    private GetUserStatusHistoryService getUserStatusHistoryService;

    @Mock
    private FindUserStatusHistoryPort findUserStatusHistoryPort;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("회원의 상태 변경 이력을 페이징 조회할 수 있다")
    void shouldReturnPagedStatusHistories() {
        // given
        UserStatusHistory history = UserStatusHistory.from(
                UserStatusHistorySnapshotState.builder()
                        .id(1L)
                        .userId(USER_ID)
                        .statusAction(UserStatusAction.DEACTIVATE)
                        .reason("욕설 사용")
                        .deactivatedUntil(LocalDateTime.of(2026, 5, 13, 0, 0))
                        .createdBy(100L)
                        .createdAt(LocalDateTime.of(2026, 4, 13, 10, 0))
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        Page<UserStatusHistory> historyPage = new PageImpl<>(List.of(history), pageable, 1);
        when(findUserStatusHistoryPort.findUserStatusHistoriesByUserId(any(Pageable.class), eq(USER_ID)))
                .thenReturn(historyPage);

        // when
        Page<GetUserStatusHistoryResult> result = getUserStatusHistoryService.getUserStatusHistories(
                USER_ID, 20, 0, Sort.Direction.DESC, UserStatusHistorySortProperty.ID
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        GetUserStatusHistoryResult firstResult = result.getContent().getFirst();
        assertThat(firstResult.id()).isEqualTo(1L);
        assertThat(firstResult.userId()).isEqualTo(USER_ID);
        assertThat(firstResult.statusAction()).isEqualTo("DEACTIVATE");
        assertThat(firstResult.reason()).isEqualTo("욕설 사용");
        assertThat(firstResult.createdBy()).isEqualTo(100L);

        verify(findUserStatusHistoryPort).findUserStatusHistoriesByUserId(any(Pageable.class), eq(USER_ID));
    }

    @Test
    @DisplayName("이력이 없는 회원은 빈 페이지를 반환한다")
    void shouldReturnEmptyPageWhenNoHistories() {
        // given
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
        Page<UserStatusHistory> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(findUserStatusHistoryPort.findUserStatusHistoriesByUserId(any(Pageable.class), eq(USER_ID)))
                .thenReturn(emptyPage);

        // when
        Page<GetUserStatusHistoryResult> result = getUserStatusHistoryService.getUserStatusHistories(
                USER_ID, 20, 0, Sort.Direction.DESC, UserStatusHistorySortProperty.ID
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}
