package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.LoginHistorySnapshotState;
import com.personal.marketnote.user.domain.user.LoginHistorySortProperty;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.result.GetLoginHistoryResult;
import com.personal.marketnote.user.port.out.user.FindLoginHistoryPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetLoginHistoryUseCaseTest {
    @Mock
    private FindLoginHistoryPort findLoginHistoryPort;

    @InjectMocks
    private GetLoginHistoryService getLoginHistoryService;

    @Test
    @DisplayName("로그인 내역 조회 시 페이징과 정렬 조건이 적용되어 결과가 매핑된다")
    void getLoginHistories_mapsResultsAndPageable() {
        // given
        Long userId = 1L;
        int pageSize = 10;
        int pageNumber = 0;
        Sort.Direction sortDirection = Sort.Direction.DESC;
        LoginHistorySortProperty sortProperty = LoginHistorySortProperty.ID;

        User user = UserTestObjectFactory.createDefaultUser(userId, EntityStatus.ACTIVE, false, List.of());
        LoginHistory history1 = buildLoginHistory(
                10L,
                user,
                AuthVendor.NATIVE,
                "127.0.0.1",
                LocalDateTime.of(2024, 1, 1, 10, 0)
        );
        LoginHistory history2 = buildLoginHistory(
                11L,
                user,
                AuthVendor.KAKAO,
                "10.0.0.2",
                LocalDateTime.of(2024, 1, 2, 11, 0)
        );

        Page<LoginHistory> histories = new PageImpl<>(
                List.of(history1, history2),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue())),
                2
        );

        when(findLoginHistoryPort.findLoginHistoriesByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(histories);

        // when
        Page<GetLoginHistoryResult> result = getLoginHistoryService.getLoginHistories(
                userId, pageSize, pageNumber, sortDirection, sortProperty
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        GetLoginHistoryResult first = result.getContent().get(0);
        assertThat(first.id()).isEqualTo(10L);
        assertThat(first.userId()).isEqualTo(userId);
        assertThat(first.authVendor()).isEqualTo(AuthVendor.NATIVE);
        assertThat(first.ipAddress()).isEqualTo("127.0.0.1");
        assertThat(first.loggedInAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));

        GetLoginHistoryResult second = result.getContent().get(1);
        assertThat(second.id()).isEqualTo(11L);
        assertThat(second.userId()).isEqualTo(userId);
        assertThat(second.authVendor()).isEqualTo(AuthVendor.KAKAO);
        assertThat(second.ipAddress()).isEqualTo("10.0.0.2");
        assertThat(second.loggedInAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 11, 0));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(findLoginHistoryPort).findLoginHistoriesByUserId(pageableCaptor.capture(), eq(userId));

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);
        Sort.Order order = pageable.getSort().getOrderFor(sortProperty.getLowerValue());
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(sortDirection);

        verifyNoMoreInteractions(findLoginHistoryPort);
    }

    @Test
    @DisplayName("로그인 내역이 없으면 빈 목록을 반환한다")
    void getLoginHistories_empty_returnsEmptyPage() {
        // given
        Long userId = 2L;
        int pageSize = 5;
        int pageNumber = 1;
        Sort.Direction sortDirection = Sort.Direction.ASC;
        LoginHistorySortProperty sortProperty = LoginHistorySortProperty.USER_ID;

        Page<LoginHistory> histories = new PageImpl<>(
                List.of(),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getLowerValue())),
                0
        );

        when(findLoginHistoryPort.findLoginHistoriesByUserId(any(Pageable.class), eq(userId)))
                .thenReturn(histories);

        // when
        Page<GetLoginHistoryResult> result = getLoginHistoryService.getLoginHistories(
                userId, pageSize, pageNumber, sortDirection, sortProperty
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(findLoginHistoryPort).findLoginHistoriesByUserId(any(Pageable.class), eq(userId));
        verifyNoMoreInteractions(findLoginHistoryPort);
    }

    private LoginHistory buildLoginHistory(
            Long id,
            User user,
            AuthVendor authVendor,
            String ipAddress,
            LocalDateTime createdAt
    ) {
        LoginHistorySnapshotState state = LoginHistorySnapshotState.builder()
                .id(id)
                .user(user)
                .authVendor(authVendor)
                .ipAddress(ipAddress)
                .createdAt(createdAt)
                .build();

        return LoginHistory.from(state);
    }
}
