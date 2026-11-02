package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoTemplateStatusTest {

    @ParameterizedTest
    @CsvSource({
            "S, 중단",
            "A, 정상",
            "R, 대기"
    })
    @DisplayName("AligoTemplateStatus.from — 유효 코드(S/A/R)를 Enum으로 변환한다")
    void shouldConvertValidCodes(String code, String expectedDescription) {
        // when
        AligoTemplateStatus status = AligoTemplateStatus.from(code);

        // then
        assertThat(status.name()).isEqualTo(code);
        assertThat(status.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("AligoTemplateStatus.from — 무효 코드 시 InvalidAligoTemplateException이 발생한다")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> AligoTemplateStatus.from("UNKNOWN"))
                .isInstanceOf(InvalidAligoTemplateException.class)
                .hasMessageContaining("알 수 없는 templateStatus");
    }

    @Test
    @DisplayName("AligoTemplateStatus.from — null 코드 시 InvalidAligoTemplateException이 발생한다")
    void shouldThrowExceptionForNullCode() {
        assertThatThrownBy(() -> AligoTemplateStatus.from(null))
                .isInstanceOf(InvalidAligoTemplateException.class);
    }

    @Test
    @DisplayName("AligoTemplateStatus.isActive — A 상태만 true를 반환한다")
    void shouldReturnTrueForActiveStatus() {
        assertThat(AligoTemplateStatus.A.isActive()).isTrue();
        assertThat(AligoTemplateStatus.S.isActive()).isFalse();
        assertThat(AligoTemplateStatus.R.isActive()).isFalse();
    }

    @Test
    @DisplayName("AligoTemplateStatus.isStopped — S 상태만 true를 반환한다")
    void shouldReturnTrueForStoppedStatus() {
        assertThat(AligoTemplateStatus.S.isStopped()).isTrue();
        assertThat(AligoTemplateStatus.A.isStopped()).isFalse();
        assertThat(AligoTemplateStatus.R.isStopped()).isFalse();
    }

    @Test
    @DisplayName("AligoTemplateStatus.isWaiting — R 상태만 true를 반환한다")
    void shouldReturnTrueForWaitingStatus() {
        assertThat(AligoTemplateStatus.R.isWaiting()).isTrue();
        assertThat(AligoTemplateStatus.A.isWaiting()).isFalse();
        assertThat(AligoTemplateStatus.S.isWaiting()).isFalse();
    }
}
