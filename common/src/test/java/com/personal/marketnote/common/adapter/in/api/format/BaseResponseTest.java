package com.personal.marketnote.common.adapter.in.api.format;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class BaseResponseTest {

    @Test
    @DisplayName("content, HttpStatus, code, message로 응답을 생성한다")
    void shouldCreateResponseWithContentHttpStatusCodeAndMessage() {
        // when
        BaseResponse<String> response = BaseResponse.of("data", HttpStatus.OK, "SUC01", "성공");

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getCode()).isEqualTo("SUC01");
        assertThat(response.getContent()).isEqualTo("data");
        assertThat(response.getMessage()).isEqualTo("성공");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("content, int statusCode, code, message로 응답을 생성한다")
    void shouldCreateResponseWithContentIntStatusCodeAndMessage() {
        // when
        BaseResponse<String> response = BaseResponse.of("data", 200, "SUC01", "성공");

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getCode()).isEqualTo("SUC01");
        assertThat(response.getContent()).isEqualTo("data");
        assertThat(response.getMessage()).isEqualTo("성공");
    }

    @Test
    @DisplayName("content, int statusCode, code로 응답을 생성하면 message는 null이다")
    void shouldCreateResponseWithNullMessageWhenMessageNotProvided() {
        // when
        BaseResponse<String> response = BaseResponse.of("data", 200, "SUC01");

        // then
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getContent()).isEqualTo("data");
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("content, HttpStatus, code로 응답을 생성하면 message는 null이다")
    void shouldCreateResponseWithHttpStatusAndNullMessage() {
        // when
        BaseResponse<String> response = BaseResponse.of("data", HttpStatus.CREATED, "SUC01");

        // then
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getContent()).isEqualTo("data");
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("int statusCode, code로 응답을 생성하면 content와 message는 null이다")
    void shouldCreateResponseWithStatusCodeAndCodeOnly() {
        // when
        BaseResponse<Object> response = BaseResponse.of(404, "ERR01");

        // then
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getCode()).isEqualTo("ERR01");
        assertThat(response.getContent()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("HttpStatus, code로 응답을 생성하면 content와 message는 null이다")
    void shouldCreateResponseWithHttpStatusAndCodeOnly() {
        // when
        BaseResponse<Object> response = BaseResponse.of(HttpStatus.NOT_FOUND, "ERR01");

        // then
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getCode()).isEqualTo("ERR01");
        assertThat(response.getContent()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("int statusCode, code, message로 응답을 생성하면 content는 null이다")
    void shouldCreateResponseWithStatusCodeCodeAndMessage() {
        // when
        BaseResponse<Object> response = BaseResponse.of(500, "ERR02", "서버 오류");

        // then
        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(response.getCode()).isEqualTo("ERR02");
        assertThat(response.getContent()).isNull();
        assertThat(response.getMessage()).isEqualTo("서버 오류");
    }

    @Test
    @DisplayName("HttpStatus, code, message로 응답을 생성하면 content는 null이다")
    void shouldCreateResponseWithHttpStatusCodeAndMessage() {
        // when
        BaseResponse<Object> response = BaseResponse.of(HttpStatus.BAD_REQUEST, "ERR03", "잘못된 요청");

        // then
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getCode()).isEqualTo("ERR03");
        assertThat(response.getContent()).isNull();
        assertThat(response.getMessage()).isEqualTo("잘못된 요청");
    }
}
