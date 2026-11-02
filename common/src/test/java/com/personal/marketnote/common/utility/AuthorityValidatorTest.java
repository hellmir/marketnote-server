package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorityValidatorTest {

    @Nested
    @DisplayName("hasAdminRole(OAuth2AuthenticatedPrincipal)")
    class HasAdminRole {

        @Test
        @DisplayName("principal이 null이면 false를 반환한다")
        void returnsFalseWhenPrincipalIsNull() {
            assertThat(AuthorityValidator.hasAdminRole(null)).isFalse();
        }

        @Test
        @DisplayName("principal에 ROLE_ADMIN 권한이 있으면 true를 반환한다")
        void returnsTrueWhenPrincipalHasAdminAuthority() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(Role.ROLE_ADMIN.name());

            assertThat(AuthorityValidator.hasAdminRole(principal)).isTrue();
        }

        @Test
        @DisplayName("principal에 ROLE_ADMIN 권한이 없으면 false를 반환한다")
        void returnsFalseWhenPrincipalHasNoAdminAuthority() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(Role.ROLE_BUYER.name());

            assertThat(AuthorityValidator.hasAdminRole(principal)).isFalse();
        }

        @Test
        @DisplayName("principal의 authorities가 비어있으면 false를 반환한다")
        void returnsFalseWhenAuthoritiesEmpty() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities();

            assertThat(AuthorityValidator.hasAdminRole(principal)).isFalse();
        }

        @Test
        @DisplayName("여러 권한 중 ROLE_ADMIN이 포함되면 true를 반환한다")
        void returnsTrueWhenAdminAmongMultipleAuthorities() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(
                    Role.ROLE_BUYER.name(), Role.ROLE_ADMIN.name()
            );

            assertThat(AuthorityValidator.hasAdminRole(principal)).isTrue();
        }
    }

    @Nested
    @DisplayName("hasSellerRole(OAuth2AuthenticatedPrincipal)")
    class HasSellerRole {

        @Test
        @DisplayName("principal이 null이면 false를 반환한다")
        void returnsFalseWhenPrincipalIsNull() {
            assertThat(AuthorityValidator.hasSellerRole(null)).isFalse();
        }

        @Test
        @DisplayName("principal에 ROLE_SELLER 권한이 있으면 true를 반환한다")
        void returnsTrueWhenPrincipalHasSellerAuthority() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(Role.ROLE_SELLER.name());

            assertThat(AuthorityValidator.hasSellerRole(principal)).isTrue();
        }

        @Test
        @DisplayName("principal에 ROLE_SELLER 권한이 없으면 false를 반환한다")
        void returnsFalseWhenPrincipalHasNoSellerAuthority() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(Role.ROLE_BUYER.name());

            assertThat(AuthorityValidator.hasSellerRole(principal)).isFalse();
        }

        @Test
        @DisplayName("principal의 authorities가 비어있으면 false를 반환한다")
        void returnsFalseWhenAuthoritiesEmpty() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities();

            assertThat(AuthorityValidator.hasSellerRole(principal)).isFalse();
        }

        @Test
        @DisplayName("여러 권한 중 ROLE_SELLER가 포함되면 true를 반환한다")
        void returnsTrueWhenSellerAmongMultipleAuthorities() {
            OAuth2AuthenticatedPrincipal principal = principalWithAuthorities(
                    Role.ROLE_BUYER.name(), Role.ROLE_SELLER.name()
            );

            assertThat(AuthorityValidator.hasSellerRole(principal)).isTrue();
        }
    }

    private OAuth2AuthenticatedPrincipal principalWithAuthorities(String... authorities) {
        List<GrantedAuthority> grantedAuthorities = List.of(authorities)
                .stream()
                .map(authority -> (GrantedAuthority) new SimpleGrantedAuthority(authority))
                .toList();
        return new DefaultOAuth2AuthenticatedPrincipal(
                "test-user",
                Map.of("sub", "test-user"),
                grantedAuthorities
        );
    }
}
