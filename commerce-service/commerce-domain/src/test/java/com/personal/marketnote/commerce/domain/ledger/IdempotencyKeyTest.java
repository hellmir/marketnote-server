package com.personal.marketnote.commerce.domain.ledger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeyTest {

    @Test
    @DisplayName("유효한 멱등성 키 문자열로 IdempotencyKey를 생성한다")
    void shouldCreateWithValidValue() {
        IdempotencyKey key = IdempotencyKey.of("PG_SETTLEMENT:123");

        assertThat(key.getValue()).isEqualTo("PG_SETTLEMENT:123");
    }

    @Test
    @DisplayName("generate(type, id)는 {type}:{id} 포맷 문자열을 반환한다")
    void shouldGenerateFormattedKey() {
        IdempotencyKey key = IdempotencyKey.generate("PG_SETTLEMENT", 456L);

        assertThat(key.getValue()).isEqualTo("PG_SETTLEMENT:456");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 IdempotencyKeyNoValueException을 던진다")
    void shouldThrowWhenValueIsBlank() {
        assertThatThrownBy(() -> IdempotencyKey.of(""))
                .isInstanceOf(IdempotencyKeyNoValueException.class);
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 IdempotencyKeyNoValueException을 던진다")
    void shouldThrowWhenValueIsWhitespaceOnly() {
        assertThatThrownBy(() -> IdempotencyKey.of("   "))
                .isInstanceOf(IdempotencyKeyNoValueException.class);
    }

    @Test
    @DisplayName("null로 생성 시 IdempotencyKeyNoValueException을 던진다")
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> IdempotencyKey.of(null))
                .isInstanceOf(IdempotencyKeyNoValueException.class);
    }

    @Test
    @DisplayName("generate 호출 시 type이 null이면 IdempotencyKeyNoValueException을 던진다")
    void shouldThrowWhenGenerateTypeIsNull() {
        assertThatThrownBy(() -> IdempotencyKey.generate(null, 100L))
                .isInstanceOf(IdempotencyKeyNoValueException.class);
    }

    @Test
    @DisplayName("generate 호출 시 id가 null이면 IdempotencyKeyNoValueException을 던진다")
    void shouldThrowWhenGenerateIdIsNull() {
        assertThatThrownBy(() -> IdempotencyKey.generate("PG_SETTLEMENT", null))
                .isInstanceOf(IdempotencyKeyNoValueException.class);
    }

    @Test
    @DisplayName("같은 값의 IdempotencyKey는 equals가 true다")
    void shouldBeEqualWhenSameValue() {
        IdempotencyKey a = IdempotencyKey.of("PG_SETTLEMENT:123");
        IdempotencyKey b = IdempotencyKey.of("PG_SETTLEMENT:123");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
