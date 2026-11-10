package com.personal.marketnote.common.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskingConverterTest {

    @Nested
    @DisplayName("email 마스킹")
    class EmailMasking {

        @Test
        @DisplayName("정상 이메일은 local-part 첫 글자 + *** + @도메인 형식으로 마스킹된다")
        void masksEmailToFirstCharAndDomain() {
            String masked = PiiMaskingConverter.mask("email=user@example.com");
            assertThat(masked).isEqualTo("email=u***@example.com");
        }

        @Test
        @DisplayName("@ 미포함 잘못된 이메일은 원본을 유지한다")
        void keepsOriginalWhenInvalidEmail() {
            String masked = PiiMaskingConverter.mask("email=invalid");
            assertThat(masked).isEqualTo("email=invalid");
        }

        @Test
        @DisplayName("maskEmail 헬퍼는 null 입력 시 null을 반환한다")
        void maskEmailReturnsNullForNull() {
            assertThat(PiiMaskingConverter.maskEmail(null)).isNull();
        }

        @Test
        @DisplayName("maskEmail 헬퍼는 빈 문자열 입력 시 빈 문자열을 반환한다")
        void maskEmailReturnsBlankForBlank() {
            assertThat(PiiMaskingConverter.maskEmail("")).isEmpty();
        }

        @Test
        @DisplayName("문장 종결 마침표가 붙은 이메일도 마스킹되고 마침표는 유지된다")
        void masksEmailWithTrailingPeriod() {
            String masked = PiiMaskingConverter.mask("email=user@example.com.");
            assertThat(masked).isEqualTo("email=u***@example.com.");
        }

        @Test
        @DisplayName("@ 이중 포함된 잘못된 이메일은 원본을 유지한다")
        void keepsOriginalForDoubleAt() {
            String masked = PiiMaskingConverter.mask("email=a@b@c.com");
            assertThat(masked).isEqualTo("email=a@b@c.com");
        }
    }

    @Nested
    @DisplayName("nickname 마스킹")
    class NicknameMasking {

        @Test
        @DisplayName("한글 닉네임은 첫 글자 + 별표로 마스킹된다")
        void masksKoreanNickname() {
            String masked = PiiMaskingConverter.mask("nickname=홍길동");
            assertThat(masked).isEqualTo("nickname=홍**");
        }

        @Test
        @DisplayName("영문 닉네임은 첫 글자 + 별표로 마스킹된다")
        void masksEnglishNickname() {
            String masked = PiiMaskingConverter.mask("nickname=alice");
            assertThat(masked).isEqualTo("nickname=a****");
        }

        @Test
        @DisplayName("한 글자 닉네임은 원본을 유지한다")
        void keepsOriginalForSingleChar() {
            assertThat(PiiMaskingConverter.maskNickname("홍")).isEqualTo("홍");
        }
    }

    @Nested
    @DisplayName("phone 마스킹")
    class PhoneMasking {

        @Test
        @DisplayName("하이픈 포함 전화번호는 가운데 4자리가 별표로 마스킹된다")
        void masksPhoneWithHyphens() {
            String masked = PiiMaskingConverter.mask("phone=010-1234-5678");
            assertThat(masked).isEqualTo("phone=010-****-5678");
        }

        @Test
        @DisplayName("하이픈 없는 전화번호도 가운데 4자리가 마스킹된 형식으로 정규화된다")
        void masksPhoneWithoutHyphens() {
            String masked = PiiMaskingConverter.mask("phone=01012345678");
            assertThat(masked).isEqualTo("phone=010-****-5678");
        }

        @Test
        @DisplayName("전화번호 형식이 아니면 원본을 유지한다")
        void keepsOriginalWhenInvalidPhone() {
            String masked = PiiMaskingConverter.mask("phone=abc");
            assertThat(masked).isEqualTo("phone=abc");
        }

        @Test
        @DisplayName("문장 종결 콜론이 붙은 전화번호도 마스킹되고 콜론은 유지된다")
        void masksPhoneWithTrailingColon() {
            String masked = PiiMaskingConverter.mask("처리 실패: phone=010-1234-5678:");
            assertThat(masked).isEqualTo("처리 실패: phone=010-****-5678:");
        }

        @Test
        @DisplayName("문장 종결 마침표가 붙은 전화번호도 마스킹되고 마침표는 유지된다")
        void masksPhoneWithTrailingPeriod() {
            String masked = PiiMaskingConverter.mask("phone=010-1234-5678.");
            assertThat(masked).isEqualTo("phone=010-****-5678.");
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "01012345678, 010-****-5678",
                "01112345678, 011-****-5678",
                "01612345678, 016-****-5678",
                "01712345678, 017-****-5678",
                "01812345678, 018-****-5678",
                "01912345678, 019-****-5678"
        })
        @DisplayName("11자리 이통사 prefix(010/011/016/017/018/019)는 중간 4자리가 마스킹된다")
        void masksElevenDigitPhoneByCarrierPrefix(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "0101234567, 010-****-4567",
                "0111234567, 011-****-4567",
                "0161234567, 016-****-4567",
                "0171234567, 017-****-4567",
                "0181234567, 018-****-4567",
                "0191234567, 019-****-4567"
        })
        @DisplayName("10자리 이통사 prefix(010/011/016/017/018/019)는 중간 3자리가 마스킹된다")
        void masksTenDigitPhoneByCarrierPrefix(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "010-123-4567, 010-****-4567",
                "02-1234-5678, 02-****-5678",
                "031-123-4567, 031-****-4567"
        })
        @DisplayName("하이픈 포함 10자리 포맷(이통사 3-3-4 / 서울 2-4-4 / 지역 3-3-4)은 중간 자릿수가 마스킹된다")
        void masksHyphenatedTenDigitPhoneByFormat(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @Test
        @DisplayName("하이픈 포함 010-1234-5678은 010-****-5678로 마스킹된다")
        void masksHyphenatedPhoneDirectly() {
            assertThat(PiiMaskingConverter.maskPhone("010-1234-5678")).isEqualTo("010-****-5678");
        }

        @Test
        @DisplayName("로그 메시지 내 하이픈 없는 phone= 토큰은 정규화된 포맷으로 마스킹된다")
        void masksNoHyphenPhoneTokenInLogMessage() {
            String masked = PiiMaskingConverter.mask("User phone=01012345678 registered");
            assertThat(masked).isEqualTo("User phone=010-****-5678 registered");
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "021234567, 02-****-4567",
                "027654321, 02-****-4321"
        })
        @DisplayName("하이픈 없는 9자리 서울 유선(02 + 7자리)은 02-****-{뒷4자리}로 마스킹된다")
        void masksNineDigitSeoulLandline(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @Test
        @DisplayName("로그 메시지 내 하이픈 없는 9자리 서울 유선 phone= 토큰은 정규화된 포맷으로 마스킹된다")
        void masksNineDigitSeoulLandlinePhoneTokenInLogMessage() {
            String masked = PiiMaskingConverter.mask("Pickup phone=021234567 confirmed");
            assertThat(masked).isEqualTo("Pickup phone=02-****-4567 confirmed");
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "0212345678, 02-****-5678",
                "0298765432, 02-****-5432"
        })
        @DisplayName("하이픈 없는 10자리 서울 유선(02 + 8자리)은 02-****-{뒷4자리}로 마스킹된다")
        void masksTenDigitSeoulLandline(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @Test
        @DisplayName("로그 메시지 내 하이픈 없는 10자리 서울 유선 phone= 토큰은 02-****-{뒷4자리}로 마스킹된다")
        void masksTenDigitSeoulLandlinePhoneTokenInLogMessage() {
            String masked = PiiMaskingConverter.mask("Office phone=0212345678 verified");
            assertThat(masked).isEqualTo("Office phone=02-****-5678 verified");
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "07012345678, 070-****-5678",
                "07087654321, 070-****-4321"
        })
        @DisplayName("11자리 070 인터넷전화는 기존대로 070-****-{뒷4자리}로 마스킹된다 (회귀 보호)")
        void masksElevenDigitInternetPhone(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource({
                "0701234567, 070-****-4567",
                "0317654321, 031-****-4321"
        })
        @DisplayName("하이픈 없는 10자리 070/031 등 3자리 prefix 지역번호는 기존 매칭으로 정상 마스킹된다 (회귀 보호)")
        void masksTenDigitNonSeoulRegionalPhone(String raw, String expected) {
            assertThat(PiiMaskingConverter.maskPhone(raw)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("name 마스킹")
    class NameMasking {

        @Test
        @DisplayName("한글 이름은 첫 글자 + 별표로 마스킹된다")
        void masksKoreanName() {
            String masked = PiiMaskingConverter.mask("name=홍길동");
            assertThat(masked).isEqualTo("name=홍**");
        }

        @Test
        @DisplayName("lastName/firstName 같은 토큰은 마스킹하지 않는다")
        void doesNotMaskSuffixedTokens() {
            String masked = PiiMaskingConverter.mask("lastName=홍길동");
            assertThat(masked).isEqualTo("lastName=홍길동");
        }

        @Test
        @DisplayName("문장 종결 물음표가 붙은 이름도 마스킹되고 물음표는 유지된다")
        void masksNameWithTrailingQuestion() {
            String masked = PiiMaskingConverter.mask("정말 name=홍길동?");
            assertThat(masked).isEqualTo("정말 name=홍**?");
        }
    }

    @Nested
    @DisplayName("복합 로그 메시지 마스킹")
    class CompositeMessage {

        @Test
        @DisplayName("email/userId/phone이 혼재한 메시지는 PII만 동시에 마스킹한다")
        void masksMultipleTokensAtOnce() {
            String masked = PiiMaskingConverter.mask(
                    "email=foo@bar.com, userId=123, phone=010-1234-5678"
            );
            assertThat(masked).isEqualTo("email=f***@bar.com, userId=123, phone=010-****-5678");
        }

        @Test
        @DisplayName("null 메시지는 null을 반환한다")
        void returnsNullForNullInput() {
            assertThat(PiiMaskingConverter.mask(null)).isNull();
        }

        @Test
        @DisplayName("빈 메시지는 빈 메시지를 반환한다")
        void returnsEmptyForEmptyInput() {
            assertThat(PiiMaskingConverter.mask("")).isEmpty();
        }

        @Test
        @DisplayName("PII 토큰이 없는 메시지는 원본을 유지한다")
        void keepsOriginalWhenNoPiiTokens() {
            String message = "order created orderId=123 totalAmount=10000";
            assertThat(PiiMaskingConverter.mask(message)).isEqualTo(message);
        }
    }
}
