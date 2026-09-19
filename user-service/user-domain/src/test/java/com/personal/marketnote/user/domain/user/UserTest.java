package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
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

            assertThatThrownBy(() -> user.validateDifferentEmail("test@example.com"))
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
