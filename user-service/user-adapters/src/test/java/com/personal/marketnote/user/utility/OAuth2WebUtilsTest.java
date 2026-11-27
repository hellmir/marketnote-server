package com.personal.marketnote.user.utility;

import com.personal.marketnote.user.port.in.result.LoginResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2WebUtilsTest {

    @Nested
    @DisplayName("buildAfterLoginRedirectionUrl - 로그인 후 리다이렉션 URL 생성")
    class BuildAfterLoginRedirectionUrlTest {

        @Test
        @DisplayName("신규 회원이고 닉네임이 있으면 user-name 쿼리 파라미터를 포함한다")
        void shouldIncludeUserNameWhenNewUserHasNickname() {
            // given
            LoginResult loginResult = LoginResult.of(true, "access-token", "refresh-token", "테스터");

            // when
            String url = OAuth2WebUtils.buildAfterLoginRedirectionUrl(
                "https://www.example.com", loginResult, "KAKAO"
            );

            // then
            assertThat(url).startsWith("https://www.example.com/redirection");
            assertThat(url).contains("type=oauth2");
            assertThat(url).contains("is-success=true");
            assertThat(url).contains("access-token=access-token");
            assertThat(url).contains("is-guest=true");
            assertThat(url).contains("auth-vendor=KAKAO");
            assertThat(url).contains("user-name=");
        }

        @Test
        @DisplayName("신규 회원이지만 닉네임이 비어있으면 user-name 쿼리 파라미터를 포함하지 않는다")
        void shouldNotIncludeUserNameWhenNewUserHasNoNickname() {
            // given
            LoginResult loginResult = LoginResult.of(true, "access-token", "refresh-token", null);

            // when
            String url = OAuth2WebUtils.buildAfterLoginRedirectionUrl(
                "https://www.example.com", loginResult, "GOOGLE"
            );

            // then
            assertThat(url).doesNotContain("user-name=");
            assertThat(url).contains("is-guest=true");
        }

        @Test
        @DisplayName("기존 회원이면 user-id 쿼리 파라미터를 빈 값으로 포함한다")
        void shouldIncludeEmptyUserIdQueryParamWhenExistingUser() {
            // given
            LoginResult loginResult = LoginResult.of(false, "access-token", "refresh-token", null);

            // when
            String url = OAuth2WebUtils.buildAfterLoginRedirectionUrl(
                "https://www.example.com", loginResult, "APPLE"
            );

            // then
            assertThat(url).contains("is-guest=false");
            assertThat(url).contains("user-id");
            assertThat(url).doesNotContain("user-name=");
        }
    }

    @Nested
    @DisplayName("extractDestination - 클라이언트 URL에서 destination 추출")
    class ExtractDestinationTest {

        @Test
        @DisplayName("유효한 https URL에서 scheme://authority를 추출한다")
        void shouldExtractSchemeAuthorityFromValidHttpsUrl() {
            // given
            String clientUrl = "https://app.example.com/login/redirect";

            // when
            String result = OAuth2WebUtils.extractDestination(clientUrl);

            // then
            assertThat(result).isEqualTo("https://app.example.com");
        }

        @Test
        @DisplayName("유효한 http URL에서 scheme://authority를 추출한다")
        void shouldExtractSchemeAuthorityFromValidHttpUrl() {
            // given
            String clientUrl = "http://localhost:8080/some/path";

            // when
            String result = OAuth2WebUtils.extractDestination(clientUrl);

            // then
            assertThat(result).isEqualTo("http://localhost:8080");
        }

        @Test
        @DisplayName("clientUrl이 null이면 null을 반환한다")
        void shouldReturnNullWhenClientUrlIsNull() {
            // when
            String result = OAuth2WebUtils.extractDestination(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("clientUrl이 빈 문자열이면 null을 반환한다")
        void shouldReturnNullWhenClientUrlIsBlank() {
            // when
            String result = OAuth2WebUtils.extractDestination("   ");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("authority가 없는 URL이면 null을 반환한다")
        void shouldReturnNullWhenAuthorityMissing() {
            // when
            String result = OAuth2WebUtils.extractDestination("/relative/path");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("패턴에 맞지 않는 scheme이면 null을 반환한다")
        void shouldReturnNullWhenSchemeNotHttpOrHttps() {
            // when
            String result = OAuth2WebUtils.extractDestination("ftp://files.example.com/file");

            // then
            assertThat(result).isNull();
        }
    }
}
