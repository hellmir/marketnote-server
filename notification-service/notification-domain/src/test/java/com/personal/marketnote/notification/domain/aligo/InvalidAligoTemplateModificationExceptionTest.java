package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidAligoTemplateModificationExceptionTest {

    @Test
    @DisplayName("InvalidAligoTemplateModificationException은 ERR_ALIGO_TEMPLATE_06 에러 코드를 포함한다")
    void shouldContainErrAligoTemplate06Code() {
        // when
        InvalidAligoTemplateModificationException exception = new InvalidAligoTemplateModificationException();

        // then
        assertThat(exception.getMessage()).startsWith("ERR_ALIGO_TEMPLATE_06::");
    }

    @Test
    @DisplayName("InvalidAligoTemplateTransitionException은 ERR_ALIGO_TEMPLATE_03 에러 코드를 포함한다")
    void shouldContainErrAligoTemplate03Code() {
        // when
        InvalidAligoTemplateTransitionException exception = new InvalidAligoTemplateTransitionException("R", "A");

        // then
        assertThat(exception.getMessage()).startsWith("ERR_ALIGO_TEMPLATE_03::");
    }

    @Test
    @DisplayName("InvalidAligoTemplateModificationException과 InvalidAligoTemplateTransitionException은 서로 다른 에러 코드 prefix를 갖는다")
    void shouldHaveDifferentErrorCodePrefixes() {
        // given
        InvalidAligoTemplateModificationException modificationException = new InvalidAligoTemplateModificationException();
        InvalidAligoTemplateTransitionException transitionException = new InvalidAligoTemplateTransitionException("R", "A");

        // when
        String modificationPrefix = modificationException.getMessage().substring(0, modificationException.getMessage().indexOf("::"));
        String transitionPrefix = transitionException.getMessage().substring(0, transitionException.getMessage().indexOf("::"));

        // then
        assertThat(modificationPrefix).isNotEqualTo(transitionPrefix);
    }
}
