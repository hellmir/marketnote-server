package com.personal.marketnote.reward.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserReferralCompletedEvent;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.DuplicateUserPointHistoryException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.GetReferralStatusUseCase;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;

import static com.personal.marketnote.common.utility.AccrualPointAmountConstant.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReferralCompletedRewardConsumerTest {
    @InjectMocks
    private UserReferralCompletedRewardConsumer userReferralCompletedRewardConsumer;

    @Mock
    private ModifyUserPointUseCase modifyUserPointUseCase;

    @Mock
    private GetReferralStatusUseCase getReferralStatusUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long requestUserId, Long referredUserId) {
        UserReferralCompletedEvent event = new UserReferralCompletedEvent(requestUserId, referredUserId);
        EventEnvelope<UserReferralCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "user.user.referral-completed", "user-service",
                LocalDateTime.of(2026, 3, 2, 10, 0), event
        );
        return new ConsumerRecord<>("user.user.referral-completed", 0, 0L, "key-1", envelope);
    }

    @Test
    @DisplayName("정상 이벤트 수신 시 추천인과 피추천인 모두 포인트 적립을 호출하고 acknowledge한다")
    void handleUserReferralCompletedEvent_success_accruesBothAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase, times(2)).modify(captor.capture());

        List<ModifyUserPointCommand> commands = captor.getAllValues();

        // 추천인 포인트 적립 (referredUserId=2에게 500원)
        ModifyUserPointCommand referrerCommand = commands.get(0);
        assertThat(referrerCommand.userId()).isEqualTo(2L);
        assertThat(referrerCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(referrerCommand.amount()).isEqualTo((long) REFERRER_USER_POINT_AMOUNT);
        assertThat(referrerCommand.sourceType()).isEqualTo(UserPointSourceType.USER);
        assertThat(referrerCommand.sourceId()).isEqualTo(1L);
        assertThat(referrerCommand.reason()).isEqualTo(REFERRER_POINT_REASON);

        // 피추천인 포인트 적립 (requestUserId=1에게 500원)
        ModifyUserPointCommand referredCommand = commands.get(1);
        assertThat(referredCommand.userId()).isEqualTo(1L);
        assertThat(referredCommand.changeType()).isEqualTo(UserPointChangeType.ACCRUAL);
        assertThat(referredCommand.amount()).isEqualTo((long) REFERRED_USER_POINT_AMOUNT);
        assertThat(referredCommand.sourceType()).isEqualTo(UserPointSourceType.USER);
        assertThat(referredCommand.sourceId()).isEqualTo(2L);
        assertThat(referredCommand.reason()).isEqualTo(REFERRED_POINT_REASON);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("requestUserId가 null이면 포인트 적립을 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_nullRequestUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 2L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("referredUserId가 null이면 포인트 적립을 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_nullReferredUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, null);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.referral-completed", 0, 0L, "1", null
        );

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        UserReferralCompletedEvent event = new UserReferralCompletedEvent(1L, 2L);
        EventEnvelope<UserReferralCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "user-service",
                LocalDateTime.of(2026, 3, 2, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "user.user.referral-completed", 0, 0L, "key-1", envelope
        );

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("requestUserId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_zeroRequestUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 2L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("requestUserId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_negativeRequestUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 2L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("referredUserId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_zeroReferredUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 0L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("referredUserId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleUserReferralCompletedEvent_negativeReferredUserId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, -1L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(modifyUserPointUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("추천인 포인트 적립 중 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleUserReferralCompletedEvent_referrerAccrualFails_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);
        doThrow(new RuntimeException("DB 연결 실패"))
                .when(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));

        // expect
        assertThatThrownBy(() ->
                userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("DB 연결 실패");

        verify(modifyUserPointUseCase).modify(any(ModifyUserPointCommand.class));
        verifyNoInteractions(acknowledgment);
    }

    @Test
    @DisplayName("피추천인 포인트 적립 중 예외 발생 시 acknowledge하지 않고 예외를 전파한다")
    void handleUserReferralCompletedEvent_referredAccrualFails_propagatesWithoutAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);
        when(modifyUserPointUseCase.modify(any(ModifyUserPointCommand.class)))
                .thenReturn(null)
                .thenThrow(new RuntimeException("포인트 잔액 부족"));

        // expect
        assertThatThrownBy(() ->
                userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("포인트 잔액 부족");

        verify(modifyUserPointUseCase, times(2)).modify(any(ModifyUserPointCommand.class));
        verifyNoInteractions(acknowledgment);
    }

    @Test
    @DisplayName("추천인 포인트 적립 중 DuplicateUserPointHistoryException 발생 시 멱등 처리하고 피추천인 적립은 정상 진행한다")
    void handleUserReferralCompletedEvent_referrerDuplicate_idempotentAndContinuesReferred() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);
        when(modifyUserPointUseCase.modify(any(ModifyUserPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(2L, UserPointSourceType.USER, 1L, REFERRER_POINT_REASON))
                .thenReturn(null);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase, times(2)).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("피추천인 포인트 적립 중 DuplicateUserPointHistoryException 발생 시 멱등 처리하고 acknowledge한다")
    void handleUserReferralCompletedEvent_referredDuplicate_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);
        when(modifyUserPointUseCase.modify(any(ModifyUserPointCommand.class)))
                .thenReturn(null)
                .thenThrow(new DuplicateUserPointHistoryException(1L, UserPointSourceType.USER, 2L, REFERRED_POINT_REASON));

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase, times(2)).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("추천인과 피추천인 모두 중복이면 멱등 처리하고 acknowledge한다")
    void handleUserReferralCompletedEvent_bothDuplicate_idempotentAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(0L);
        when(modifyUserPointUseCase.modify(any(ModifyUserPointCommand.class)))
                .thenThrow(new DuplicateUserPointHistoryException(2L, UserPointSourceType.USER, 1L, REFERRER_POINT_REASON))
                .thenThrow(new DuplicateUserPointHistoryException(1L, UserPointSourceType.USER, 2L, REFERRED_POINT_REASON));

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase, times(2)).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("최대 초대 수(20명)에 도달한 추천인은 추천인 포인트를 적립하지 않고 피추천인만 적립한다")
    void handleUserReferralCompletedEvent_maxReached_skipsReferrerAndAccruesReferred() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(20L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase, times(1)).modify(captor.capture());

        ModifyUserPointCommand referredCommand = captor.getValue();
        assertThat(referredCommand.userId()).isEqualTo(1L);
        assertThat(referredCommand.reason()).isEqualTo(REFERRED_POINT_REASON);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Consumer에서 보너스 자동 적립 없이 기본 500캐시만 적립한다")
    void handleUserReferralCompletedEvent_tierMilestone_noBonusOnlyBasicAccrual() {
        // given — 초대 수가 정확히 5가 되는 시점 (기존에는 보너스 자동 적립 발생)
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(4L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then — 보너스 없이 기본 적립만 2회 (추천인 500 + 피추천인 500)
        ArgumentCaptor<ModifyUserPointCommand> captor = ArgumentCaptor.forClass(ModifyUserPointCommand.class);
        verify(modifyUserPointUseCase, times(2)).modify(captor.capture());

        List<ModifyUserPointCommand> commands = captor.getAllValues();

        // 추천인 기본 적립 500원
        assertThat(commands.get(0).userId()).isEqualTo(2L);
        assertThat(commands.get(0).amount()).isEqualTo((long) REFERRER_USER_POINT_AMOUNT);
        assertThat(commands.get(0).reason()).isEqualTo(REFERRER_POINT_REASON);

        // 피추천인 기본 적립 500원
        assertThat(commands.get(1).userId()).isEqualTo(1L);
        assertThat(commands.get(1).amount()).isEqualTo((long) REFERRED_USER_POINT_AMOUNT);
        assertThat(commands.get(1).reason()).isEqualTo(REFERRED_POINT_REASON);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("보너스 단계에 해당하지 않는 초대 수이면 기본 적립만 수행한다")
    void handleUserReferralCompletedEvent_nonTierCount_noBonusAccrued() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 2L);
        when(getReferralStatusUseCase.countCompletedReferrals(2L)).thenReturn(6L);

        // when
        userReferralCompletedRewardConsumer.handleUserReferralCompletedEvent(record, acknowledgment);

        // then
        verify(modifyUserPointUseCase, times(2)).modify(any(ModifyUserPointCommand.class));
        verify(acknowledgment).acknowledge();
    }
}
