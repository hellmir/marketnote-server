package com.personal.marketnote.common.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuthenticationDefaultEntryPointTest {

    @InjectMocks
    private AuthenticationDefaultEntryPoint entryPoint;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("인증 실패 시 응답 상태 코드를 401로 설정한다")
    void shouldSetResponseStatusTo401() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException authException = new BadCredentialsException("Invalid credentials");

        // when
        entryPoint.commence(request, response, authException);

        // then
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("인증 실패 시 응답 Content-Type을 application/json으로 설정한다")
    void shouldSetContentTypeToApplicationJson() throws IOException, ServletException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException authException = new BadCredentialsException("Invalid credentials");

        // when
        entryPoint.commence(request, response, authException);

        // then
        assertThat(response.getContentType()).isEqualTo("application/json");
    }
}
