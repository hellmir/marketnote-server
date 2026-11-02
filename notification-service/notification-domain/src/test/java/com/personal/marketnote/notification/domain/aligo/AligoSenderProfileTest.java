package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoSenderProfileTest {

    @Test
    @DisplayName("CreateState로 생성 시 유효한 senderKey/plusId를 가지면 활성 상태로 초기화된다")
    void shouldCreateFromValidCreateState() {
        // given
        AligoSenderProfileCreateState state = validCreateState().build();

        // when
        AligoSenderProfile profile = AligoSenderProfile.from(state);

        // then
        assertThat(profile.getSenderKey()).isEqualTo("sender-key-001");
        assertThat(profile.getPlusId()).isEqualTo("@plus-id");
        assertThat(profile.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(profile.getProfileInspectionStatus()).isEqualTo(AligoProfileInspectionStatus.A);
        assertThat(profile.getSenderProfileStatus()).isEqualTo(AligoSenderProfileStatus.A);
        assertThat(profile.isActive()).isTrue();
    }

    @Test
    @DisplayName("CreateState.senderKey가 NULL이면 InvalidAligoSenderProfileException이 발생한다")
    void shouldThrowExceptionWhenSenderKeyIsNull() {
        // given
        AligoSenderProfileCreateState state = validCreateState().senderKey(null).build();

        // when & then
        assertThatThrownBy(() -> AligoSenderProfile.from(state))
                .isInstanceOf(InvalidAligoSenderProfileException.class)
                .hasMessageContaining("senderKey는 필수");
    }

    @Test
    @DisplayName("CreateState.senderKey가 빈 문자열이면 InvalidAligoSenderProfileException이 발생한다")
    void shouldThrowExceptionWhenSenderKeyIsBlank() {
        // given
        AligoSenderProfileCreateState state = validCreateState().senderKey("  ").build();

        // when & then
        assertThatThrownBy(() -> AligoSenderProfile.from(state))
                .isInstanceOf(InvalidAligoSenderProfileException.class)
                .hasMessageContaining("senderKey는 필수");
    }

    @Test
    @DisplayName("CreateState.plusId가 NULL이면 InvalidAligoSenderProfileException이 발생한다")
    void shouldThrowExceptionWhenPlusIdIsNull() {
        // given
        AligoSenderProfileCreateState state = validCreateState().plusId(null).build();

        // when & then
        assertThatThrownBy(() -> AligoSenderProfile.from(state))
                .isInstanceOf(InvalidAligoSenderProfileException.class)
                .hasMessageContaining("plusId는 필수");
    }

    @Test
    @DisplayName("SnapshotState로 복원 시 EntityStatus를 포함한 모든 필드가 복원된다")
    void shouldRestoreFromSnapshotState() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modifiedAt = LocalDateTime.of(2026, 1, 2, 0, 0);
        AligoSenderProfileSnapshotState state = AligoSenderProfileSnapshotState.builder()
                .id(10L)
                .senderKey("sender-key-001")
                .plusId("@plus-id")
                .phoneNumber("01012345678")
                .categoryCode("002001")
                .name("테스트 프로필")
                .licenseNum("000-00-00000")
                .profileInspectionStatus(AligoProfileInspectionStatus.A)
                .senderProfileStatus(AligoSenderProfileStatus.A)
                .alimUseYn(true)
                .uuid("uuid-test")
                .status(EntityStatus.INACTIVE)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

        // when
        AligoSenderProfile profile = AligoSenderProfile.from(state);

        // then
        assertThat(profile.getId()).isEqualTo(10L);
        assertThat(profile.getSenderKey()).isEqualTo("sender-key-001");
        assertThat(profile.getPlusId()).isEqualTo("@plus-id");
        assertThat(profile.getCreatedAt()).isEqualTo(createdAt);
        assertThat(profile.getModifiedAt()).isEqualTo(modifiedAt);
        assertThat(profile.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(profile.isActive()).isFalse();
    }

    @Test
    @DisplayName("updateFromExternal은 profileInspectionStatus/senderProfileStatus/alimUseYn 3개 필드만 업데이트한다")
    void shouldUpdateThreeFieldsFromExternal() {
        // given
        AligoSenderProfile profile = AligoSenderProfile.from(validCreateState().build());

        // when
        profile.updateFromExternal(
                AligoProfileInspectionStatus.B,
                AligoSenderProfileStatus.S,
                false
        );

        // then
        assertThat(profile.getProfileInspectionStatus()).isEqualTo(AligoProfileInspectionStatus.B);
        assertThat(profile.getSenderProfileStatus()).isEqualTo(AligoSenderProfileStatus.S);
        assertThat(profile.isAlimUseYn()).isFalse();
        // 나머지 필드는 변경되지 않음
        assertThat(profile.getSenderKey()).isEqualTo("sender-key-001");
        assertThat(profile.getPlusId()).isEqualTo("@plus-id");
        assertThat(profile.getPhoneNumber()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("isInspectionApproved — profileInspectionStatus가 A이면 true, 그 외는 false")
    void shouldDelegateIsInspectionApprovedToProfileInspectionStatus() {
        // given
        AligoSenderProfile approved = AligoSenderProfile.from(
                validCreateState().profileInspectionStatus(AligoProfileInspectionStatus.A).build()
        );
        AligoSenderProfile blocked = AligoSenderProfile.from(
                validCreateState().profileInspectionStatus(AligoProfileInspectionStatus.B).build()
        );
        AligoSenderProfile denied = AligoSenderProfile.from(
                validCreateState().profileInspectionStatus(AligoProfileInspectionStatus.D).build()
        );

        // expect
        assertThat(approved.isInspectionApproved()).isTrue();
        assertThat(blocked.isInspectionApproved()).isFalse();
        assertThat(denied.isInspectionApproved()).isFalse();
    }

    @Test
    @DisplayName("isProfileActive — senderProfileStatus가 A이면 true, 그 외는 false")
    void shouldDelegateIsProfileActiveToSenderProfileStatus() {
        // given
        AligoSenderProfile active = AligoSenderProfile.from(
                validCreateState().senderProfileStatus(AligoSenderProfileStatus.A).build()
        );
        AligoSenderProfile stopped = AligoSenderProfile.from(
                validCreateState().senderProfileStatus(AligoSenderProfileStatus.S).build()
        );
        AligoSenderProfile deleted = AligoSenderProfile.from(
                validCreateState().senderProfileStatus(AligoSenderProfileStatus.D).build()
        );

        // expect
        assertThat(active.isProfileActive()).isTrue();
        assertThat(stopped.isProfileActive()).isFalse();
        assertThat(deleted.isProfileActive()).isFalse();
    }

    @Test
    @DisplayName("updateCategoryCode는 카테고리 코드만 변경한다")
    void shouldUpdateCategoryCode() {
        // given
        AligoSenderProfile profile = AligoSenderProfile.from(validCreateState().build());

        // when
        profile.updateCategoryCode("002002");

        // then
        assertThat(profile.getCategoryCode()).isEqualTo("002002");
    }

    private AligoSenderProfileCreateState.AligoSenderProfileCreateStateBuilder validCreateState() {
        return AligoSenderProfileCreateState.builder()
                .senderKey("sender-key-001")
                .plusId("@plus-id")
                .phoneNumber("01012345678")
                .categoryCode("002001")
                .name("테스트 프로필")
                .licenseNum("000-00-00000")
                .profileInspectionStatus(AligoProfileInspectionStatus.A)
                .senderProfileStatus(AligoSenderProfileStatus.A)
                .alimUseYn(true)
                .uuid("uuid-test");
    }
}
