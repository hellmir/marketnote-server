package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkFailoverTypeTest {

    @ParameterizedTest
    @CsvSource({
            "SMS, SMS",
            "LMS, LMS",
            "MMS, MMS"
    })
    @DisplayName("BrandTalkFailoverType.from — 유효 코드(SMS/LMS/MMS)를 Enum으로 변환한다")
    void shouldConvertValidCodes(String code, String expectedDescription) {
        // when
        BrandTalkFailoverType type = BrandTalkFailoverType.from(code);

        // then
        assertThat(type.name()).isEqualTo(code);
        assertThat(type.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("BrandTalkFailoverType.from — 무효 코드 시 InvalidBrandTalkFailoverException이 발생한다")
    void shouldThrowExceptionForInvalidCode() {
        assertThatThrownBy(() -> BrandTalkFailoverType.from("XMS"))
                .isInstanceOf(InvalidBrandTalkFailoverException.class)
                .hasMessageContaining("알 수 없는 대체 발송 유형");
    }

    @Test
    @DisplayName("BrandTalkFailoverType.from — null 코드 시 InvalidBrandTalkFailoverException이 발생한다")
    void shouldThrowExceptionForNullCode() {
        assertThatThrownBy(() -> BrandTalkFailoverType.from(null))
                .isInstanceOf(InvalidBrandTalkFailoverException.class);
    }

    @Test
    @DisplayName("BrandTalkFailoverType.isMms — MMS 타입만 true를 반환한다")
    void shouldReturnTrueOnlyForMms() {
        assertThat(BrandTalkFailoverType.MMS.isMms()).isTrue();
        assertThat(BrandTalkFailoverType.SMS.isMms()).isFalse();
        assertThat(BrandTalkFailoverType.LMS.isMms()).isFalse();
    }
}
