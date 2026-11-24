package com.personal.marketnote.common.utility.http.client.restclient;

import com.personal.marketnote.common.logging.TraceIdMdcFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceIdPropagationInterceptorTest {

    private final TraceIdPropagationInterceptor interceptor = new TraceIdPropagationInterceptor();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC에 requestId가 있고 호출자가 헤더를 설정하지 않았으면 X-Request-ID 헤더를 추가한다")
    void shouldAddTraceIdHeaderWhenMdcHasValueAndHeaderAbsent() throws IOException {
        // given
        String traceId = "trace-id-12345";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, traceId);

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        byte[] body = new byte[0];

        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, body)).thenReturn(response);

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        assertThat(headers.getFirst(TraceIdMdcFilter.REQUEST_ID_HEADER)).isEqualTo(traceId);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("MDC가 비어 있으면 X-Request-ID 헤더를 추가하지 않는다")
    void shouldNotAddHeaderWhenMdcEmpty() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(execution.execute(request, body)).thenReturn(response);

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        verify(request, never()).getHeaders();
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("호출자가 이미 X-Request-ID 헤더를 설정했으면 덮어쓰지 않는다")
    void shouldNotOverwriteWhenCallerAlreadySetHeader() throws IOException {
        // given
        String mdcTraceId = "mdc-trace-id";
        String callerTraceId = "caller-trace-id";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, mdcTraceId);

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(TraceIdMdcFilter.REQUEST_ID_HEADER, callerTraceId);
        byte[] body = new byte[0];

        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, body)).thenReturn(response);

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        assertThat(headers.getFirst(TraceIdMdcFilter.REQUEST_ID_HEADER)).isEqualTo(callerTraceId);
        assertThat(headers.get(TraceIdMdcFilter.REQUEST_ID_HEADER)).hasSize(1);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("MDC requestId가 빈 문자열이면 헤더를 추가하지 않는다")
    void shouldNotAddHeaderWhenMdcTraceIdBlank() throws IOException {
        // given
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "   ");

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(execution.execute(request, body)).thenReturn(response);

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        verify(request, never()).getHeaders();
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("정상 케이스에서 execution.execute를 정확히 1회 호출하고 그 응답을 반환한다")
    void shouldDelegateExecutionExactlyOnce() throws IOException {
        // given
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, "trace-once");

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        byte[] body = new byte[0];

        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, body)).thenReturn(response);

        // when
        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        // then
        verify(execution, times(1)).execute(request, body);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("예외가 발생해도 MDC를 변경하지 않는다")
    void shouldPreserveMdcWhenExceptionOccurs() throws IOException {
        // given
        String traceId = "preserve-trace";
        MDC.put(TraceIdMdcFilter.REQUEST_ID_MDC_KEY, traceId);

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        HttpHeaders headers = new HttpHeaders();
        byte[] body = new byte[0];

        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, body)).thenThrow(new IOException("Connection refused"));

        // when & then
        assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
                .isInstanceOf(IOException.class)
                .hasMessage("Connection refused");
        assertThat(MDC.get(TraceIdMdcFilter.REQUEST_ID_MDC_KEY)).isEqualTo(traceId);
    }
}
