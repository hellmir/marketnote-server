package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoSenderProfileStatusTest {

    @ParameterizedTest
    @CsvSource({
            "A, 활성",
            "S, 중지",
            "D, 삭제"
    })
    @DisplayName("AligoSenderProfileStatus.from — 유효 코드(A/S/D)를 Enum으로 변환한다")
    void shouldConvertValidCodes(String code, String expectedDescription) {
        // when
        AligoSenderProfileStatus status = AligoSenderProfileStatus.from(code);

        // then
        assertThat(status.name()).isEqualTo(code);
        assertThat(status.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("AligoSenderProfileStatus.from — 무효 코드 시 InvalidAligoSenderProfileException이 발생한다")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> AligoSenderProfileStatus.from("X"))
                .isInstanceOf(InvalidAligoSenderProfileException.class)
                .hasMessageContaining("알 수 없는 senderProfileStatus");
    }

    @Test
    @DisplayName("AligoSenderProfileStatus.isActive — A 상태만 true를 반환한다")
    void shouldReturnTrueForActive() {
        assertThat(AligoSenderProfileStatus.A.isActive()).isTrue();
        assertThat(AligoSenderProfileStatus.S.isActive()).isFalse();
        assertThat(AligoSenderProfileStatus.D.isActive()).isFalse();
    }

    @Test
    @DisplayName("AligoSenderProfileStatus.isDeleted — D 상태만 true를 반환한다")
    void shouldReturnTrueForDeleted() {
        assertThat(AligoSenderProfileStatus.D.isDeleted()).isTrue();
        assertThat(AligoSenderProfileStatus.A.isDeleted()).isFalse();
        assertThat(AligoSenderProfileStatus.S.isDeleted()).isFalse();
    }
}
