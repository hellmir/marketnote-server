package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.domain.exception.illegalstate.SameUpdateTargetException;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.exception.ReferredUserCodeAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Nested
    @DisplayName("from(UserSnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("ACTIVE 상태의 UserSnapshotState로 복원하면 status가 ACTIVE이다")
        void shouldRestoreWithActiveStatus() {
            User user = createUserWithStatus(EntityStatus.ACTIVE);

            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("INACTIVE 상태의 UserSnapshotState로 복원하면 status가 INACTIVE이다")
        void shouldRestoreWithInactiveStatus() {
            User user = createUserWithStatus(EntityStatus.INACTIVE);

            assertThat(user.isInactive()).isTrue();
        }

        @Test
        @DisplayName("UNEXPOSED 상태의 UserSnapshotState로 복원하면 status가 UNEXPOSED이다")
        void shouldRestoreWithUnexposedStatus() {
            User user = createUserWithStatus(EntityStatus.UNEXPOSED);

            assertThat(user.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("withdraw 호출 시 withdrawalYn이 true가 되고 비활성화된다")
        void shouldSetWithdrawalYnTrueAndDeactivate() {
            User user = createActiveUser();
            LocalDateTime now = LocalDateTime.of(2026, 4, 11, 10, 0);

            user.withdraw(now);

            assertThat(user.isWithdrawn()).isTrue();
            assertThat(user.isInactive()).isTrue();
            assertThat(user.getWithdrawnAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("이미 탈퇴한 사용자에게 withdraw를 호출하면 변경 없다")
        void shouldNotChangeWhenAlreadyWithdrawn() {
            User user = createActiveUser();
            LocalDateTime firstWithdrawTime = LocalDateTime.of(2026, 4, 1, 10, 0);
            user.withdraw(firstWithdrawTime);

            LocalDateTime secondWithdrawTime = LocalDateTime.of(2026, 4, 11, 10, 0);
            user.withdraw(secondWithdrawTime);

            assertThat(user.getWithdrawnAt()).isEqualTo(firstWithdrawTime);
        }
    }

    @Nested
    @DisplayName("cancelWithdrawal")
    class CancelWithdrawal {

        @Test
        @DisplayName("cancelWithdrawal 호출 시 withdrawalYn이 false가 되고 활성화된다")
        void shouldSetWithdrawalYnFalseAndActivate() {
            User user = createActiveUser();
            LocalDateTime withdrawTime = LocalDateTime.of(2026, 4, 1, 10, 0);
            user.withdraw(withdrawTime);

            LocalDateTime cancelTime = LocalDateTime.of(2026, 4, 5, 10, 0);
            user.cancelWithdrawal(cancelTime);

            assertThat(user.isWithdrawn()).isFalse();
            assertThat(user.isActive()).isTrue();
            assertThat(user.getWithdrawnAt()).isNull();
            assertThat(user.getSignedUpAt()).isEqualTo(cancelTime);
        }
    }

    @Nested
    @DisplayName("isSelfReferral")
    class IsSelfReferral {

        @Test
        @DisplayName("자신의 referenceCode와 동일한 코드를 전달하면 true를 반환한다")
        void shouldReturnTrueWhenCodeMatchesReferenceCode() {
            User user = createUserWithReferenceCode("ABC123");

            assertThat(user.isSelfReferral("ABC123")).isTrue();
        }

        @Test
        @DisplayName("다른 코드를 전달하면 false를 반환한다")
        void shouldReturnFalseWhenCodeDiffers() {
            User user = createUserWithReferenceCode("ABC123");

            assertThat(user.isSelfReferral("XYZ789")).isFalse();
        }
    }

    @Nested
    @DisplayName("isMutualReferralWith")
    class IsMutualReferralWith {

        @Test
        @DisplayName("상호 추천 관계이면 true를 반환한다")
        void shouldReturnTrueWhenMutualReferral() {
            User userA = createUserWithReferredUserCode("CODE_B");
            User userB = createUserWithReferenceCode("CODE_B");

            assertThat(userA.isMutualReferralWith(userB)).isTrue();
        }

        @Test
        @DisplayName("상호 추천 관계가 아니면 false를 반환한다")
        void shouldReturnFalseWhenNotMutualReferral() {
            User userA = createUserWithReferredUserCode("CODE_B");
            User userB = createUserWithReferenceCode("CODE_C");

            assertThat(userA.isMutualReferralWith(userB)).isFalse();
        }
    }

    @Nested
    @DisplayName("registerReferredUserCode")
    class RegisterReferredUserCode {

        @Test
        @DisplayName("추천코드 미등록 상태에서 정상 등록된다")
        void shouldRegisterWhenNotAlreadyRegistered() {
            User user = createActiveUser();

            user.registerReferredUserCode("INVITED_CODE");

            assertThat(user.getReferredUserCode()).isEqualTo("INVITED_CODE");
        }

        @Test
        @DisplayName("이미 등록된 상태에서 호출하면 ReferredUserCodeAlreadyExistsException이 발생한다")
        void shouldThrowWhenAlreadyRegistered() {
            User user = createUserWithReferredUserCode("EXISTING_CODE");

            assertThatThrownBy(() -> user.registerReferredUserCode("NEW_CODE"))
                    .isInstanceOf(ReferredUserCodeAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("validateDifferent")
    class ValidateDifferent {

        @Test
        @DisplayName("동일 이메일이면 SameUpdateTargetException이 발생한다")
        void shouldThrowWhenSameEmail() {
            User user = createUserWithEmail("test@example.com");

            assertThatThrownBy(() -> user.validateDifferentEmail(Email.of("test@example.com")))
                    .isInstanceOf(SameUpdateTargetException.class);
        }

        @Test
        @DisplayName("동일 닉네임이면 SameUpdateTargetException이 발생한다")
        void shouldThrowWhenSameNickname() {
            User user = createUserWithNickname("홍길동");

            assertThatThrownBy(() -> user.validateDifferentNickname("홍길동"))
                    .isInstanceOf(SameUpdateTargetException.class);
        }

        @Test
        @DisplayName("동일 전화번호이면 SameUpdateTargetException이 발생한다")
        void shouldThrowWhenSamePhoneNumber() {
            User user = createUserWithPhoneNumber("010-1234-5678");

            assertThatThrownBy(() -> user.validateDifferentPhoneNumber(PhoneNumber.of("010-1234-5678")))
                    .isInstanceOf(SameUpdateTargetException.class);
        }
    }

    @Nested
    @DisplayName("canReSignUp")
    class CanReSignUp {

        @Test
        @DisplayName("탈퇴 후 30일 경과하면 true를 반환한다")
        void shouldReturnTrueWhenCooldownPassed() {
            User user = createWithdrawnUser(LocalDateTime.of(2026, 3, 1, 10, 0));

            LocalDateTime now = LocalDateTime.of(2026, 4, 1, 10, 0);

            assertThat(user.canReSignUp(now)).isTrue();
        }

        @Test
        @DisplayName("탈퇴 후 30일 미경과하면 false를 반환한다")
        void shouldReturnFalseWhenCooldownNotPassed() {
            User user = createWithdrawnUser(LocalDateTime.of(2026, 4, 1, 10, 0));

            LocalDateTime now = LocalDateTime.of(2026, 4, 10, 10, 0);

            assertThat(user.canReSignUp(now)).isFalse();
        }

        @Test
        @DisplayName("withdrawnAt이 null이면 true를 반환한다")
        void shouldReturnTrueWhenWithdrawnAtIsNull() {
            User user = createActiveUser();

            LocalDateTime now = LocalDateTime.of(2026, 4, 11, 10, 0);

            assertThat(user.canReSignUp(now)).isTrue();
        }
    }

    @Nested
    @DisplayName("isWithdrawn")
    class IsWithdrawn {

        @Test
        @DisplayName("탈퇴 상태이면 true를 반환한다")
        void shouldReturnTrueWhenWithdrawn() {
            User user = createActiveUser();
            user.withdraw(LocalDateTime.now());

            assertThat(user.isWithdrawn()).isTrue();
        }

        @Test
        @DisplayName("비탈퇴 상태이면 false를 반환한다")
        void shouldReturnFalseWhenNotWithdrawn() {
            User user = createActiveUser();

            assertThat(user.isWithdrawn()).isFalse();
        }
    }

    @Nested
    @DisplayName("addPenalty")
    class AddPenalty {

        @Test
        @DisplayName("addPenalty 호출 시 penaltyCount가 1 증가한다")
        void shouldIncreasePenaltyCountByOne() {
            User user = createUserWithPenaltyCount(0);

            user.addPenalty();

            assertThat(user.getPenaltyCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("addPenalty를 여러 번 호출하면 penaltyCount가 누적된다")
        void shouldAccumulatePenaltyCountOnMultipleCalls() {
            User user = createUserWithPenaltyCount(0);

            user.addPenalty();
            user.addPenalty();
            user.addPenalty();

            assertThat(user.getPenaltyCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("기존 패널티 횟수가 있는 회원에 addPenalty 호출 시 누적된다")
        void shouldAccumulateOnExistingPenaltyCount() {
            User user = createUserWithPenaltyCount(5);

            user.addPenalty();

            assertThat(user.getPenaltyCount()).isEqualTo(6);
        }

        @Test
        @DisplayName("UserSnapshotState로 복원된 User의 penaltyCount는 주입된 값과 일치한다")
        void shouldRestorePenaltyCountFromSnapshot() {
            User user = createUserWithPenaltyCount(7);

            assertThat(user.getPenaltyCount()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("updatePenaltyCount")
    class UpdatePenaltyCount {

        @Test
        @DisplayName("updatePenaltyCount 호출 시 penaltyCount가 요청값으로 설정된다")
        void shouldSetPenaltyCountToRequestedValue() {
            User user = createUserWithPenaltyCount(3);

            user.updatePenaltyCount(10);

            assertThat(user.getPenaltyCount()).isEqualTo(10);
        }

        @Test
        @DisplayName("updatePenaltyCount로 0을 설정하면 penaltyCount가 0이 된다")
        void shouldResetPenaltyCountToZero() {
            User user = createUserWithPenaltyCount(5);

            user.updatePenaltyCount(0);

            assertThat(user.getPenaltyCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("validateDifferentPenaltyCount")
    class ValidateDifferentPenaltyCount {

        @Test
        @DisplayName("다른 penaltyCount면 예외가 발생하지 않는다")
        void shouldNotThrowWhenDifferent() {
            User user = createUserWithPenaltyCount(3);

            user.validateDifferentPenaltyCount(5);
        }

        @Test
        @DisplayName("동일한 penaltyCount면 SameUpdateTargetException이 발생한다")
        void shouldThrowWhenSame() {
            User user = createUserWithPenaltyCount(3);

            assertThatThrownBy(() -> user.validateDifferentPenaltyCount(3))
                    .isInstanceOf(SameUpdateTargetException.class);
        }
    }

    private User createUserWithPenaltyCount(int penaltyCount) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .penaltyCount(penaltyCount)
                .build());
    }

    private User createActiveUser() {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .nickname("테스트유저")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithStatus(EntityStatus status) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(status)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithReferenceCode(String referenceCode) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .referenceCode(referenceCode)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithReferredUserCode(String referredUserCode) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .referredUserCode(referredUserCode)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithEmail(String email) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .email(email)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithNickname(String nickname) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .nickname(nickname)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createUserWithPhoneNumber(String phoneNumber) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .phoneNumber(phoneNumber)
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.ACTIVE)
                .withdrawalYn(false)
                .build());
    }

    private User createWithdrawnUser(LocalDateTime withdrawnAt) {
        return User.from(UserSnapshotState.builder()
                .id(1L)
                .userKey(UUID.randomUUID())
                .role(Role.getBuyer())
                .userAuthProviders(new ArrayList<>())
                .userTerms(List.of())
                .status(EntityStatus.INACTIVE)
                .withdrawalYn(true)
                .withdrawnAt(withdrawnAt)
                .build());
    }
}
