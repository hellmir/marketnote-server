package com.personal.marketnote.common.utility.http.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CommunicationFailureHandlerTest {

    @Test
    @DisplayName("응답이 null이면 실패로 판단하지 않는다")
    void shouldReturnFalseWhenResponseIsNull() {
        // when
        boolean result = CommunicationFailureHandler.isCertainFailure(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("응답 상태가 200이면 실패가 아니라고 판단한다")
    void shouldReturnTrueWhenStatusIs200() {
        // given
        ResponseEntity<String> response = new ResponseEntity<>("ok", HttpStatus.OK);

        // when
        boolean result = CommunicationFailureHandler.isCertainFailure(response);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("응답 상태가 200이 아니면 실패로 판단하지 않는다")
    void shouldReturnFalseWhenStatusIsNot200() {
        // given
        ResponseEntity<String> response = new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);

        // when
        boolean result = CommunicationFailureHandler.isCertainFailure(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("응답 상태가 404이면 실패로 판단하지 않는다")
    void shouldReturnFalseWhenStatusIs404() {
        // given
        ResponseEntity<String> response = new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);

        // when
        boolean result = CommunicationFailureHandler.isCertainFailure(response);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("응답 본문이 null이어도 상태가 200이면 실패가 아니라고 판단한다")
    void shouldReturnTrueWhenStatusIs200WithNullBody() {
        // given
        ResponseEntity<String> response = new ResponseEntity<>(null, HttpStatus.OK);

        // when
        boolean result = CommunicationFailureHandler.isCertainFailure(response);

        // then
        assertThat(result).isTrue();
    }
}
