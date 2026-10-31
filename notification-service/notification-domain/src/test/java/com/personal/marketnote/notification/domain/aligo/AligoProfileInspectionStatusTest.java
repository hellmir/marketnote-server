package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoProfileInspectionStatusTest {

    @ParameterizedTest
    @CsvSource({
            "A, 승인",
            "C, 신청",
            "B, 차단",
            "E, 삭제",
            "D, 거부"
    })
    @DisplayName("AligoProfileInspectionStatus.from — 유효 코드(A/C/B/E/D)를 Enum으로 변환한다")
    void shouldConvertValidCodes(String code, String expectedDescription) {
        // when
        AligoProfileInspectionStatus status = AligoProfileInspectionStatus.from(code);

        // then
        assertThat(status.name()).isEqualTo(code);
        assertThat(status.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("AligoProfileInspectionStatus.from — 무효 코드 시 InvalidAligoSenderProfileException이 발생한다")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> AligoProfileInspectionStatus.from("X"))
                .isInstanceOf(InvalidAligoSenderProfileException.class)
                .hasMessageContaining("알 수 없는 profileInspectionStatus");
    }

    @Test
    @DisplayName("AligoProfileInspectionStatus.isApproved — A 상태만 true를 반환한다")
    void shouldReturnTrueForApproved() {
        assertThat(AligoProfileInspectionStatus.A.isApproved()).isTrue();
        assertThat(AligoProfileInspectionStatus.C.isApproved()).isFalse();
        assertThat(AligoProfileInspectionStatus.B.isApproved()).isFalse();
        assertThat(AligoProfileInspectionStatus.E.isApproved()).isFalse();
        assertThat(AligoProfileInspectionStatus.D.isApproved()).isFalse();
    }

    @Test
    @DisplayName("AligoProfileInspectionStatus.isBlocked — B 상태만 true를 반환한다")
    void shouldReturnTrueForBlocked() {
        assertThat(AligoProfileInspectionStatus.B.isBlocked()).isTrue();
        assertThat(AligoProfileInspectionStatus.A.isBlocked()).isFalse();
        assertThat(AligoProfileInspectionStatus.C.isBlocked()).isFalse();
        assertThat(AligoProfileInspectionStatus.E.isBlocked()).isFalse();
        assertThat(AligoProfileInspectionStatus.D.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("AligoProfileInspectionStatus.isDenied — D 상태만 true를 반환한다")
    void shouldReturnTrueForDenied() {
        assertThat(AligoProfileInspectionStatus.D.isDenied()).isTrue();
        assertThat(AligoProfileInspectionStatus.A.isDenied()).isFalse();
        assertThat(AligoProfileInspectionStatus.B.isDenied()).isFalse();
        assertThat(AligoProfileInspectionStatus.C.isDenied()).isFalse();
        assertThat(AligoProfileInspectionStatus.E.isDenied()).isFalse();
    }
}
