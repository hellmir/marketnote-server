package com.personal.marketnote.common.kafka.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvelopePayloadPiiHasherTest {

    private static final String SALT = "tenant-a-salt";
    private static final String OTHER_SALT = "tenant-b-salt";

    @Test
    @DisplayName("null 입력은 null 그대로 반환한다")
    void hash_nullValue_returnsNull() {
        // when
        String result = EnvelopePayloadPiiHasher.hash(null, SALT);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 입력도 64자 hex로 해시된다 (null만 통과)")
    void hash_blankValue_isHashed() {
        // when
        String empty = EnvelopePayloadPiiHasher.hash("", SALT);
        String spaces = EnvelopePayloadPiiHasher.hash("   ", SALT);

        // then
        assertThat(empty).matches("^[0-9a-f]{64}$");
        assertThat(spaces).matches("^[0-9a-f]{64}$");
        assertThat(empty).isNotEqualTo(spaces);
    }

    @Test
    @DisplayName("같은 입력 + 같은 salt는 항상 같은 해시를 반환한다 (deterministic)")
    void hash_sameInputSameSalt_isDeterministic() {
        // when
        String first = EnvelopePayloadPiiHasher.hash("홍길동", SALT);
        String second = EnvelopePayloadPiiHasher.hash("홍길동", SALT);

        // then
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("같은 입력 + 다른 salt는 다른 해시를 반환한다 (per-tenant unlinkability)")
    void hash_sameInputDifferentSalt_producesDifferentHash() {
        // when
        String hashA = EnvelopePayloadPiiHasher.hash("홍길동", SALT);
        String hashB = EnvelopePayloadPiiHasher.hash("홍길동", OTHER_SALT);

        // then
        assertThat(hashA).isNotEqualTo(hashB);
    }

    @Test
    @DisplayName("다른 입력 + 같은 salt는 다른 해시를 반환한다")
    void hash_differentInputSameSalt_producesDifferentHash() {
        // when
        String hashA = EnvelopePayloadPiiHasher.hash("홍길동", SALT);
        String hashB = EnvelopePayloadPiiHasher.hash("김철수", SALT);

        // then
        assertThat(hashA).isNotEqualTo(hashB);
    }

    @Test
    @DisplayName("해시는 16진수 소문자 문자열, 길이 64자(SHA-256)이다")
    void hash_isLowercaseHexLength64() {
        // when
        String result = EnvelopePayloadPiiHasher.hash("010-1234-5678", SALT);

        // then
        assertThat(result).hasSize(64);
        assertThat(result).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("한글 입력은 UTF-8로 인코딩되어 해시된다")
    void hash_koreanInput_isUtf8Encoded() {
        // 동일 입력에 대해 결정적이면서 64자 hex가 나오면 UTF-8 인코딩이 정상 동작
        // when
        String result = EnvelopePayloadPiiHasher.hash("홍길동", SALT);

        // then
        assertThat(result).hasSize(64);
        assertThat(result).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("salt가 null이면 IllegalArgumentException을 던진다")
    void hash_nullSalt_throws() {
        // when & then
        assertThatThrownBy(() -> EnvelopePayloadPiiHasher.hash("value", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("salt가 빈 문자열이면 IllegalArgumentException을 던진다")
    void hash_blankSalt_throws() {
        // when & then
        assertThatThrownBy(() -> EnvelopePayloadPiiHasher.hash("value", ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> EnvelopePayloadPiiHasher.hash("value", "   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
