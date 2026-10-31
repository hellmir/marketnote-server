package com.personal.marketnote.user.security.token.support;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.common.domain.exception.token.InvalidRefreshTokenException;
import com.personal.marketnote.common.domain.exception.token.UnsupportedCodeException;
import com.personal.marketnote.user.security.token.dto.GrantedTokenInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.dto.OAuth2UserInfo;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegatingTokenSupportTest {

    @Mock
    private TokenProcessor kakaoProcessor;

    @Mock
    private TokenProcessor googleProcessor;

    @Mock
    private TokenProcessor appleProcessor;

    private DelegatingTokenSupport createDelegating(TokenProcessor... processors) {
        for (TokenProcessor processor : processors) {
            if (processor == kakaoProcessor) {
                when(processor.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
                continue;
            }
            if (processor == googleProcessor) {
                when(processor.getAuthVendor()).thenReturn(AuthVendor.GOOGLE);
                continue;
            }
            if (processor == appleProcessor) {
                when(processor.getAuthVendor()).thenReturn(AuthVendor.APPLE);
            }
        }
        return new DelegatingTokenSupport(List.of(processors));
    }

    @Nested
    @DisplayName("grantToken - 공급자별 토큰 발급 위임")
    class GrantTokenTest {

        @Test
        @DisplayName("요청 공급자와 매칭되는 프로세서로 토큰 발급을 위임한다")
        void shouldDelegateToMatchingProcessor() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            GrantedTokenInfo expected = GrantedTokenInfo.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .id("oidc-1")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.grantToken("auth-code", "https://example.com/callback"))
                    .thenReturn(expected);

            // when
            GrantedTokenInfo actual = delegatingTokenSupport.grantToken(
                    "auth-code", "https://example.com/callback", AuthVendor.KAKAO
            );

            // then
            assertThat(actual).isSameAs(expected);
            verify(kakaoProcessor).grantToken("auth-code", "https://example.com/callback");
            verify(googleProcessor, never()).grantToken("auth-code", "https://example.com/callback");
        }

        @Test
        @DisplayName("매칭되는 프로세서가 없으면 UnsupportedCodeException을 던진다")
        void shouldThrowUnsupportedCodeExceptionWhenNoProcessorMatches() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor);

            // when & then
            assertThatThrownBy(() -> delegatingTokenSupport.grantToken(
                    "auth-code", "https://example.com/callback", AuthVendor.GOOGLE
            ))
                    .isInstanceOf(UnsupportedCodeException.class)
                    .hasMessageContaining("GOOGLE");
        }
    }

    @Nested
    @DisplayName("authenticate - 다중 공급자 순회 인증")
    class AuthenticateTest {

        @Test
        @DisplayName("첫 번째 프로세서가 성공하면 그 결과를 반환하고 다음 프로세서는 호출하지 않는다")
        void shouldReturnFirstMatchedResult() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            OAuth2AuthenticationInfo expected = OAuth2AuthenticationInfo.builder()
                    .id("oidc-1")
                    .authVendor(AuthVendor.KAKAO)
                    .userId(10L)
                    .build();
            when(kakaoProcessor.authenticate("access-token")).thenReturn(expected);

            // when
            OAuth2AuthenticationInfo actual = delegatingTokenSupport.authenticate("access-token");

            // then
            assertThat(actual).isSameAs(expected);
            verify(kakaoProcessor).authenticate("access-token");
            verify(googleProcessor, never()).authenticate("access-token");
        }

        @Test
        @DisplayName("앞선 프로세서가 실패해도 다음 프로세서가 성공하면 그 결과를 반환한다")
        void shouldTryNextProcessorWhenPreviousFails() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            when(kakaoProcessor.authenticate("access-token"))
                    .thenThrow(new InvalidAccessTokenException("kakao failed"));
            OAuth2AuthenticationInfo googleResult = OAuth2AuthenticationInfo.builder()
                    .id("oidc-2")
                    .authVendor(AuthVendor.GOOGLE)
                    .userId(20L)
                    .build();
            when(googleProcessor.authenticate("access-token")).thenReturn(googleResult);

            // when
            OAuth2AuthenticationInfo actual = delegatingTokenSupport.authenticate("access-token");

            // then
            assertThat(actual).isSameAs(googleResult);
            verify(kakaoProcessor).authenticate("access-token");
            verify(googleProcessor).authenticate("access-token");
        }

        @Test
        @DisplayName("모든 프로세서가 실패하면 InvalidAccessTokenException을 던진다")
        void shouldThrowWhenAllProcessorsFail() {
            // given
            DelegatingTokenSupport delegatingTokenSupport =
                    createDelegating(kakaoProcessor, googleProcessor, appleProcessor);

            when(kakaoProcessor.authenticate("access-token"))
                    .thenThrow(new InvalidAccessTokenException("kakao failed"));
            when(googleProcessor.authenticate("access-token"))
                    .thenThrow(new InvalidAccessTokenException("google failed"));
            when(appleProcessor.authenticate("access-token"))
                    .thenThrow(new InvalidAccessTokenException("apple failed"));

            // when & then
            assertThatThrownBy(() -> delegatingTokenSupport.authenticate("access-token"))
                    .isInstanceOf(InvalidAccessTokenException.class);

            verify(kakaoProcessor).authenticate("access-token");
            verify(googleProcessor).authenticate("access-token");
            verify(appleProcessor).authenticate("access-token");
        }
    }

    @Nested
    @DisplayName("retrieveUserInfo - 다중 공급자 순회 사용자 정보 조회")
    class RetrieveUserInfoTest {

        @Test
        @DisplayName("첫 번째 프로세서가 성공하면 그 결과를 반환하고 다음 프로세서는 호출하지 않는다")
        void shouldReturnFirstMatchedResult() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            OAuth2UserInfo expected = OAuth2UserInfo.builder().id("oidc-1").name("kakao-user").build();
            when(kakaoProcessor.retrieveUserInfo("access-token")).thenReturn(expected);

            // when
            OAuth2UserInfo actual = delegatingTokenSupport.retrieveUserInfo("access-token");

            // then
            assertThat(actual).isSameAs(expected);
            verify(kakaoProcessor).retrieveUserInfo("access-token");
            verify(googleProcessor, never()).retrieveUserInfo("access-token");
        }

        @Test
        @DisplayName("모든 프로세서가 실패하면 InvalidAccessTokenException을 던진다")
        void shouldThrowWhenAllProcessorsFail() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            when(kakaoProcessor.retrieveUserInfo("access-token"))
                    .thenThrow(new InvalidAccessTokenException("kakao failed"));
            when(googleProcessor.retrieveUserInfo("access-token"))
                    .thenThrow(new InvalidAccessTokenException("google failed"));

            // when & then
            assertThatThrownBy(() -> delegatingTokenSupport.retrieveUserInfo("access-token"))
                    .isInstanceOf(InvalidAccessTokenException.class);
        }
    }

    @Nested
    @DisplayName("refreshToken - 다중 공급자 순회 리프레시")
    class RefreshTokenTest {

        @Test
        @DisplayName("첫 번째 프로세서가 성공하면 그 결과를 반환한다")
        void shouldReturnFirstMatchedResult() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            GrantedTokenInfo expected = GrantedTokenInfo.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .id("oidc-1")
                    .authVendor(AuthVendor.KAKAO)
                    .build();
            when(kakaoProcessor.refreshToken("refresh-token")).thenReturn(expected);

            // when
            GrantedTokenInfo actual = delegatingTokenSupport.refreshToken("refresh-token");

            // then
            assertThat(actual).isSameAs(expected);
            verify(kakaoProcessor).refreshToken("refresh-token");
            verify(googleProcessor, never()).refreshToken("refresh-token");
        }

        @Test
        @DisplayName("앞선 프로세서가 실패해도 다음 프로세서가 성공하면 그 결과를 반환한다")
        void shouldTryNextProcessorWhenPreviousFails() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            when(kakaoProcessor.refreshToken("refresh-token"))
                    .thenThrow(new InvalidRefreshTokenException("kakao failed"));
            GrantedTokenInfo googleResult = GrantedTokenInfo.builder()
                    .accessToken("google-access")
                    .refreshToken("google-refresh")
                    .id("oidc-2")
                    .authVendor(AuthVendor.GOOGLE)
                    .build();
            when(googleProcessor.refreshToken("refresh-token")).thenReturn(googleResult);

            // when
            GrantedTokenInfo actual = delegatingTokenSupport.refreshToken("refresh-token");

            // then
            assertThat(actual).isSameAs(googleResult);
            verify(kakaoProcessor).refreshToken("refresh-token");
            verify(googleProcessor).refreshToken("refresh-token");
        }

        @Test
        @DisplayName("모든 프로세서가 실패하면 InvalidRefreshTokenException을 던진다")
        void shouldThrowWhenAllProcessorsFail() {
            // given
            DelegatingTokenSupport delegatingTokenSupport = createDelegating(kakaoProcessor, googleProcessor);

            when(kakaoProcessor.refreshToken("refresh-token"))
                    .thenThrow(new InvalidRefreshTokenException("kakao failed"));
            when(googleProcessor.refreshToken("refresh-token"))
                    .thenThrow(new InvalidRefreshTokenException("google failed"));

            // when & then
            assertThatThrownBy(() -> delegatingTokenSupport.refreshToken("refresh-token"))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }
}
