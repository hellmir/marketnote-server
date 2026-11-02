package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkTemplateStatusTest {

    @ParameterizedTest
    @CsvSource({
            "O, NORMAL, 정상",
            "S, STOPPED, 중단",
            "R, WAITING, 대기"
    })
    @DisplayName("BrandTalkTemplateStatus.from — 유효 코드(O/S/R)를 Enum으로 변환한다")
    void shouldConvertValidCodes(String code, String expectedName, String expectedDescription) {
        // when
        BrandTalkTemplateStatus status = BrandTalkTemplateStatus.from(code);

        // then
        assertThat(status.name()).isEqualTo(expectedName);
        assertThat(status.getCode()).isEqualTo(code);
        assertThat(status.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("BrandTalkTemplateStatus.from — 무효 코드 시 InvalidBrandTalkTemplateStatusException이 발생한다")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> BrandTalkTemplateStatus.from("Z"))
                .isInstanceOf(InvalidBrandTalkTemplateStatusException.class)
                .hasMessageContaining("알 수 없는 브랜드메시지 템플릿 상태");
    }

    @Test
    @DisplayName("BrandTalkTemplateStatus.from — null 코드 시 InvalidBrandTalkTemplateStatusException이 발생한다")
    void shouldThrowExceptionForNullCode() {
        assertThatThrownBy(() -> BrandTalkTemplateStatus.from(null))
                .isInstanceOf(InvalidBrandTalkTemplateStatusException.class);
    }

    @Test
    @DisplayName("BrandTalkTemplateStatus.isNormal — NORMAL 상태만 true를 반환한다")
    void shouldReturnTrueForNormal() {
        assertThat(BrandTalkTemplateStatus.NORMAL.isNormal()).isTrue();
        assertThat(BrandTalkTemplateStatus.STOPPED.isNormal()).isFalse();
        assertThat(BrandTalkTemplateStatus.WAITING.isNormal()).isFalse();
    }

    @Test
    @DisplayName("BrandTalkTemplateStatus.isStopped — STOPPED 상태만 true를 반환한다")
    void shouldReturnTrueForStopped() {
        assertThat(BrandTalkTemplateStatus.STOPPED.isStopped()).isTrue();
        assertThat(BrandTalkTemplateStatus.NORMAL.isStopped()).isFalse();
        assertThat(BrandTalkTemplateStatus.WAITING.isStopped()).isFalse();
    }

    @Test
    @DisplayName("BrandTalkTemplateStatus.isWaiting — WAITING 상태만 true를 반환한다")
    void shouldReturnTrueForWaiting() {
        assertThat(BrandTalkTemplateStatus.WAITING.isWaiting()).isTrue();
        assertThat(BrandTalkTemplateStatus.NORMAL.isWaiting()).isFalse();
        assertThat(BrandTalkTemplateStatus.STOPPED.isWaiting()).isFalse();
    }
}
