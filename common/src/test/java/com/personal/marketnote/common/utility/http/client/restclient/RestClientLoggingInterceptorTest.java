package com.personal.marketnote.common.utility.http.client.restclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestClientLoggingInterceptorTest {

    private final RestClientLoggingInterceptor interceptor = new RestClientLoggingInterceptor();

    @Test
    @DisplayName("정상 요청 시 응답을 그대로 반환한다")
    void shouldReturnResponseWhenRequestSucceeds() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test"));
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("4xx 응답 시에도 응답을 그대로 반환한다")
    void shouldReturnResponseWhen4xxStatus() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.POST);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test"));
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(404));

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("5xx 응답 시에도 응답을 그대로 반환한다")
    void shouldReturnResponseWhen5xxStatus() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.PUT);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test"));
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("요청 실행 중 IOException 발생 시 예외를 다시 던진다")
    void shouldRethrowExceptionWhenRequestFails() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        byte[] body = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.DELETE);
        when(request.getURI()).thenReturn(URI.create("http://localhost:8080/api/test"));
        when(execution.execute(request, body)).thenThrow(new IOException("Connection refused"));

        // when & then
        assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
                .isInstanceOf(IOException.class)
                .hasMessage("Connection refused");
    }
}
