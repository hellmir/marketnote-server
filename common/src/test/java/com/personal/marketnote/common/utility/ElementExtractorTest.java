package com.personal.marketnote.common.utility;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@DisplayName("ElementExtractor 테스트")
class ElementExtractorTest {

    @Nested
    @DisplayName("extractRole 테스트")
    class ExtractRoleTest {

        @Test
        @DisplayName("ROLE_BUYER authority가 있으면 BUYER를 반환한다")
        void shouldReturnBuyerWhenRoleBuyerAuthorityExists() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_BUYER"));
            doReturn(authorities).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("ROLE_ADMIN authority가 있으면 ADMIN을 반환한다")
        void shouldReturnAdminWhenRoleAdminAuthorityExists() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            doReturn(authorities).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("ROLE_SELLER authority가 있으면 SELLER를 반환한다")
        void shouldReturnSellerWhenRoleSellerAuthorityExists() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SELLER"));
            doReturn(authorities).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("SELLER");
        }

        @Test
        @DisplayName("principal이 null이면 기본값 BUYER를 반환한다")
        void shouldReturnBuyerWhenPrincipalIsNull() {
            String role = ElementExtractor.extractRole(null);

            assertThat(role).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("authorities가 비어있으면 기본값 BUYER를 반환한다")
        void shouldReturnBuyerWhenAuthoritiesEmpty() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            doReturn(Collections.emptyList()).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("ROLE_ 접두사가 없는 authority만 있으면 기본값 BUYER를 반환한다")
        void shouldReturnBuyerWhenNoRolePrefixedAuthority() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("SCOPE_read"),
                    new SimpleGrantedAuthority("SCOPE_write")
            );
            doReturn(authorities).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("BUYER");
        }

        @Test
        @DisplayName("여러 ROLE_ authority가 있으면 첫 번째를 반환한다")
        void shouldReturnFirstRoleWhenMultipleRolesExist() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            Collection<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_SELLER")
            );
            doReturn(authorities).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("authorities가 null이면 기본값 BUYER를 반환한다")
        void shouldReturnBuyerWhenAuthoritiesNull() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            doReturn(null).when(principal).getAuthorities();

            String role = ElementExtractor.extractRole(principal);

            assertThat(role).isEqualTo("BUYER");
        }
    }
}
