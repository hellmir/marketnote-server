package com.personal.marketnote.user.adapter.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sha256HashUtils")
class Sha256HashUtilsTest {

    @Nested
    @DisplayName("encode")
    class Encode {

        @Test
        @DisplayName("동일 입력에 대해 동일 해시를 반환한다")
        void shouldReturnSameHashForSameInput() {
            String input = "user-fingerprint-abc";

            String hash1 = Sha256HashUtils.encode(input);
            String hash2 = Sha256HashUtils.encode(input);

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("서로 다른 입력은 다른 해시를 생성한다")
        void shouldReturnDifferentHashForDifferentInput() {
            String hashA = Sha256HashUtils.encode("input-a");
            String hashB = Sha256HashUtils.encode("input-b");

            assertThat(hashA).isNotEqualTo(hashB);
        }

        @Test
        @DisplayName("SHA-256 결과는 64자 소문자 16진수 문자열이다")
        void shouldReturn64HexCharacters() {
            String hash = Sha256HashUtils.encode("any-value");

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("[0-9a-f]{64}");
        }

        @Test
        @DisplayName("빈 문자열의 SHA-256 해시는 RFC 표준값과 일치한다")
        void shouldMatchKnownHashForEmptyString() {
            String hash = Sha256HashUtils.encode("");

            assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        }

        @Test
        @DisplayName("'abc' 입력의 SHA-256 해시는 NIST 공식 테스트 벡터와 일치한다")
        void shouldMatchKnownHashForAbc() {
            String hash = Sha256HashUtils.encode("abc");

            assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        }

        @Test
        @DisplayName("한글 입력도 UTF-8 기반으로 일관되게 해시된다")
        void shouldHashKoreanConsistently() {
            String hashFirst = Sha256HashUtils.encode("테스트문자열");
            String hashSecond = Sha256HashUtils.encode("테스트문자열");

            assertThat(hashFirst).isEqualTo(hashSecond);
            assertThat(hashFirst).hasSize(64);
        }
    }
}
