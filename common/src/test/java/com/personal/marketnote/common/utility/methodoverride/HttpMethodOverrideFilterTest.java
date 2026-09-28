package com.personal.marketnote.common.utility.methodoverride;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpMethodOverrideFilterTest {

    @InjectMocks
    private HttpMethodOverrideFilter httpMethodOverrideFilter;

    @Test
    @DisplayName("X-HTTP-Method-Override 헤더가 없으면 원본 요청을 그대로 전달한다")
    void shouldPassOriginalRequestWhenNoOverrideHeader() throws IOException, ServletException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(HttpMethodOverrideConstant.HEADER_NAME)).thenReturn(null);

        // when
        httpMethodOverrideFilter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-HTTP-Method-Override 헤더가 빈 문자열이면 원본 요청을 그대로 전달한다")
    void shouldPassOriginalRequestWhenOverrideHeaderIsEmpty() throws IOException, ServletException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(HttpMethodOverrideConstant.HEADER_NAME)).thenReturn("");

        // when
        httpMethodOverrideFilter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-HTTP-Method-Override 헤더가 PATCH이면 요청 메서드를 PATCH로 오버라이드한다")
    void shouldOverrideMethodToPatchWhenHeaderIsPatch() throws IOException, ServletException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(HttpMethodOverrideConstant.HEADER_NAME)).thenReturn("PATCH");

        // when
        httpMethodOverrideFilter.doFilter(request, response, filterChain);

        // then
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = (HttpServletRequest) requestCaptor.getValue();
        assertThat(wrappedRequest.getMethod()).isEqualTo("PATCH");
    }

    @Test
    @DisplayName("X-HTTP-Method-Override 헤더가 DELETE이면 요청 메서드를 DELETE로 오버라이드한다")
    void shouldOverrideMethodToDeleteWhenHeaderIsDelete() throws IOException, ServletException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(HttpMethodOverrideConstant.HEADER_NAME)).thenReturn("DELETE");

        // when
        httpMethodOverrideFilter.doFilter(request, response, filterChain);

        // then
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = (HttpServletRequest) requestCaptor.getValue();
        assertThat(wrappedRequest.getMethod()).isEqualTo("DELETE");
    }

    @Test
    @DisplayName("X-HTTP-Method-Override 헤더가 PUT이면 요청 메서드를 PUT으로 오버라이드한다")
    void shouldOverrideMethodToPutWhenHeaderIsPut() throws IOException, ServletException {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader(HttpMethodOverrideConstant.HEADER_NAME)).thenReturn("PUT");

        // when
        httpMethodOverrideFilter.doFilter(request, response, filterChain);

        // then
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = (HttpServletRequest) requestCaptor.getValue();
        assertThat(wrappedRequest.getMethod()).isEqualTo("PUT");
    }
}
