package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusActor;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.domain.user.UserStatusHistorySnapshotState;
import com.personal.marketnote.user.port.in.result.ActivateExpiredDeactivationResult;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.SaveUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateExpiredDeactivationUseCaseTest {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 4, 13, 12, 0);
    private final Clock fixedClock = Clock.fixed(NOW.atZone(KST).toInstant(), KST);

    @Mock
    private FindUserPort findUserPort;

    @Mock
    private UpdateUserPort updateUserPort;

    @Mock
    private SaveUserStatusHistoryPort saveUserStatusHistoryPort;

    private ActivateExpiredDeactivationService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ActivateExpiredDeactivationService(
                fixedClock, findUserPort, updateUserPort, saveUserStatusHistoryPort
        );
    }

    @Test
    @DisplayName("만료된 비활성화 회원이 없으면 0건을 반환하고 update/save가 호출되지 않는다")
    void shouldReturnZeroWhenNoExpiredUsers() {
        // given
        when(findUserPort.findAllByDeactivatedUntilExpired(NOW)).thenReturn(List.of());

        // when
        ActivateExpiredDeactivationResult result = service.activateExpiredDeactivations();

        // then
        assertThat(result.activatedCount()).isZero();
        assertThat(result.activatedUserIds()).isEmpty();

        verify(findUserPort).findAllByDeactivatedUntilExpired(NOW);
        verifyNoInteractions(updateUserPort);
        verifyNoInteractions(saveUserStatusHistoryPort);
    }

    @Test
    @DisplayName("만료된 비활성화 회원 1명을 활성화하고 SYSTEM actor 이력을 저장한다")
    void shouldActivateSingleExpiredUser() {
        // given
        Long userId = 10L;
        User inactiveUser = createInactiveUserWithDeactivatedUntil(
                userId, NOW.minusDays(1)
        );
        when(findUserPort.findAllByDeactivatedUntilExpired(NOW)).thenReturn(List.of(inactiveUser));
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ActivateExpiredDeactivationResult result = service.activateExpiredDeactivations();

        // then
        assertThat(result.activatedCount()).isEqualTo(1);
        assertThat(result.activatedUserIds()).containsExactly(userId);
        assertThat(inactiveUser.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(inactiveUser.getDeactivatedUntil()).isNull();

        verify(updateUserPort).update(inactiveUser);

        ArgumentCaptor<UserStatusHistory> historyCaptor = ArgumentCaptor.forClass(UserStatusHistory.class);
        verify(saveUserStatusHistoryPort).save(historyCaptor.capture());
        UserStatusHistory captured = historyCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getStatusAction()).isEqualTo(UserStatusAction.ACTIVATE);
        assertThat(captured.getActor()).isEqualTo(UserStatusActor.SYSTEM);
        assertThat(captured.getCreatedBy()).isNull();
        assertThat(captured.getDeactivatedUntil()).isNull();
    }

    @Test
    @DisplayName("만료된 비활성화 회원 여러 명을 모두 활성화한다")
    void shouldActivateMultipleExpiredUsers() {
        // given
        User user1 = createInactiveUserWithDeactivatedUntil(11L, NOW.minusDays(2));
        User user2 = createInactiveUserWithDeactivatedUntil(12L, NOW.minusHours(1));
        when(findUserPort.findAllByDeactivatedUntilExpired(NOW)).thenReturn(List.of(user1, user2));
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        ActivateExpiredDeactivationResult result = service.activateExpiredDeactivations();

        // then
        assertThat(result.activatedCount()).isEqualTo(2);
        assertThat(result.activatedUserIds()).containsExactly(11L, 12L);
        assertThat(user1.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(user2.getStatus()).isEqualTo(EntityStatus.ACTIVE);

        verify(updateUserPort).update(user1);
        verify(updateUserPort).update(user2);
        verify(saveUserStatusHistoryPort, times(2)).save(any(UserStatusHistory.class));
    }

    @Test
    @DisplayName("개별 회원 처리 실패 시에도 다른 회원 처리는 계속된다")
    void shouldContinueWhenIndividualUserFails() {
        // given
        User user1 = createInactiveUserWithDeactivatedUntil(20L, NOW.minusDays(1));
        User user2 = createInactiveUserWithDeactivatedUntil(21L, NOW.minusDays(1));
        when(findUserPort.findAllByDeactivatedUntilExpired(NOW)).thenReturn(List.of(user1, user2));
        when(saveUserStatusHistoryPort.save(any(UserStatusHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        org.mockito.Mockito.doThrow(new RuntimeException("DB error"))
                .when(updateUserPort).update(user1);

        // when
        ActivateExpiredDeactivationResult result = service.activateExpiredDeactivations();

        // then
        assertThat(result.activatedCount()).isEqualTo(1);
        assertThat(result.activatedUserIds()).containsExactly(21L);

        verify(updateUserPort).update(user1);
        verify(updateUserPort).update(user2);
        verify(saveUserStatusHistoryPort, times(1)).save(any(UserStatusHistory.class));
    }

    private User createInactiveUserWithDeactivatedUntil(Long id, LocalDateTime deactivatedUntil) {
        return UserTestObjectFactory.createUser(
                id,
                "user" + id,
                "user" + id + "@test.com",
                "테스터" + id,
                "010-9000-000" + (id % 10),
                "ref-" + id,
                com.personal.marketnote.user.domain.authentication.Role.getBuyer(),
                List.of(),
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                EntityStatus.INACTIVE,
                false,
                0L,
                deactivatedUntil
        );
    }
}
