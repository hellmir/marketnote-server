package com.personal.marketnote.user.security.token.introspector;

import com.personal.marketnote.common.domain.exception.token.InvalidAccessTokenException;
import com.personal.marketnote.user.constant.PrimaryRole;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.dto.OAuth2AuthenticationInfo;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpaqueTokenDefaultIntrospectorTest {
    private static final String KAKAO_ISSUER = "https://kauth.kakao.com";
    private static final String OPAQUE_TOKEN = "opaque-access-token";
    private static final String NATIVE_OIDC_ID = "oidc-native-123";
    private static final String KAKAO_OIDC_ID = "oidc-kakao-456";

    @Mock
    private TokenSupport tokenSupport;
    @Mock
    private FindUserPort findUserPort;

    private OpaqueTokenDefaultIntrospector introspector;

    @BeforeEach
    void setUp() {
        Map<AuthVendor, List<String>> vendorIssuerMap = Map.of(
                AuthVendor.KAKAO, List.of(KAKAO_ISSUER)
        );
        introspector = new OpaqueTokenDefaultIntrospector(tokenSupport, findUserPort, vendorIssuerMap);
    }

    @Test
    @DisplayName("활성 회원의 토큰 검증 시 사용자 role principal을 반환한다")
    void introspectActiveUserReturnsUserPrincipal() {
        // given
        OAuth2AuthenticationInfo info = OAuth2AuthenticationInfo.builder()
                .id(NATIVE_OIDC_ID)
                .authVendor(AuthVendor.NATIVE)
                .userId(1L)
                .build();
        User activeUser = mock(User.class);
        when(activeUser.getId()).thenReturn(1L);
        when(activeUser.isActive()).thenReturn(true);
        when(activeUser.getRole()).thenReturn(Role.getBuyer());

        when(tokenSupport.authenticate(OPAQUE_TOKEN)).thenReturn(info);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.NATIVE, NATIVE_OIDC_ID))
                .thenReturn(Optional.of(activeUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(OPAQUE_TOKEN);

        // then
        assertThat(principal.getName()).isEqualTo("1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_BUYER");
    }

    @Test
    @DisplayName("비활성 회원의 토큰 검증 시 ANONYMOUS principal을 반환한다")
    void introspectInactiveUserReturnsAnonymousPrincipal() {
        // given
        OAuth2AuthenticationInfo info = OAuth2AuthenticationInfo.builder()
                .id(NATIVE_OIDC_ID)
                .authVendor(AuthVendor.NATIVE)
                .userId(1L)
                .build();
        User inactiveUser = mock(User.class);
        when(inactiveUser.isActive()).thenReturn(false);

        when(tokenSupport.authenticate(OPAQUE_TOKEN)).thenReturn(info);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.NATIVE, NATIVE_OIDC_ID))
                .thenReturn(Optional.of(inactiveUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(OPAQUE_TOKEN);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_ANONYMOUS.name());
    }

    @Test
    @DisplayName("비노출 회원의 토큰 검증 시 ANONYMOUS principal을 반환한다")
    void introspectUnexposedUserReturnsAnonymousPrincipal() {
        // given
        OAuth2AuthenticationInfo info = OAuth2AuthenticationInfo.builder()
                .id(NATIVE_OIDC_ID)
                .authVendor(AuthVendor.NATIVE)
                .userId(1L)
                .build();
        User unexposedUser = mock(User.class);
        when(unexposedUser.isActive()).thenReturn(false);

        when(tokenSupport.authenticate(OPAQUE_TOKEN)).thenReturn(info);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.NATIVE, NATIVE_OIDC_ID))
                .thenReturn(Optional.of(unexposedUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(OPAQUE_TOKEN);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_ANONYMOUS.name());
    }

    @Test
    @DisplayName("활성 회원의 벤더 ID 토큰 검증 시 사용자 role principal을 반환한다")
    void introspectVendorIdTokenForActiveUserReturnsUserPrincipal() {
        // given
        String vendorToken = buildVendorIdToken(KAKAO_ISSUER, KAKAO_OIDC_ID);
        User activeUser = mock(User.class);
        when(activeUser.getId()).thenReturn(2L);
        when(activeUser.isActive()).thenReturn(true);
        when(activeUser.getRole()).thenReturn(Role.getBuyer());

        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, KAKAO_OIDC_ID))
                .thenReturn(Optional.of(activeUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(vendorToken);

        // then
        assertThat(principal.getName()).isEqualTo("2");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_BUYER");
    }

    @Test
    @DisplayName("비활성 회원의 벤더 ID 토큰 검증 시 ANONYMOUS principal을 반환한다")
    void introspectVendorIdTokenForInactiveUserReturnsAnonymousPrincipal() {
        // given
        String vendorToken = buildVendorIdToken(KAKAO_ISSUER, KAKAO_OIDC_ID);
        User inactiveUser = mock(User.class);
        when(inactiveUser.isActive()).thenReturn(false);

        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, KAKAO_OIDC_ID))
                .thenReturn(Optional.of(inactiveUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(vendorToken);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_ANONYMOUS.name());
    }

    @Test
    @DisplayName("비노출 회원의 벤더 ID 토큰 검증 시 ANONYMOUS principal을 반환한다")
    void introspectVendorIdTokenForUnexposedUserReturnsAnonymousPrincipal() {
        // given
        String vendorToken = buildVendorIdToken(KAKAO_ISSUER, KAKAO_OIDC_ID);
        User unexposedUser = mock(User.class);
        when(unexposedUser.isActive()).thenReturn(false);

        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.KAKAO, KAKAO_OIDC_ID))
                .thenReturn(Optional.of(unexposedUser));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(vendorToken);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_ANONYMOUS.name());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 GUEST principal을 반환한다")
    void introspectUserNotFoundReturnsGuestPrincipal() {
        // given
        OAuth2AuthenticationInfo info = OAuth2AuthenticationInfo.builder()
                .id("oidc-unknown")
                .authVendor(AuthVendor.NATIVE)
                .userId(99L)
                .build();

        when(tokenSupport.authenticate(OPAQUE_TOKEN)).thenReturn(info);
        when(findUserPort.findAllStatusUserByAuthVendorAndOidcId(AuthVendor.NATIVE, "oidc-unknown"))
                .thenReturn(Optional.empty());
        when(findUserPort.findAllStatusUserById(99L)).thenReturn(Optional.empty());

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(OPAQUE_TOKEN);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_GUEST.name());
    }

    @Test
    @DisplayName("InvalidAccessTokenException 발생 시 ANONYMOUS principal을 반환한다")
    void introspectInvalidTokenReturnsAnonymousPrincipal() {
        // given
        when(tokenSupport.authenticate(OPAQUE_TOKEN))
                .thenThrow(new InvalidAccessTokenException("invalid"));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(OPAQUE_TOKEN);

        // then
        assertThat(principal.getName()).isEqualTo("-1");
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(PrimaryRole.ROLE_ANONYMOUS.name());
    }

    private String buildVendorIdToken(String issuer, String subject) {
        String header = base64UrlEncode("{\"typ\":\"JWT\",\"alg\":\"RS256\"}");
        String payload = base64UrlEncode(
                "{\"iss\":\"" + issuer + "\",\"sub\":\"" + subject + "\"}"
        );
        String signature = base64UrlEncode("fake-signature");
        return header + "." + payload + "." + signature;
    }

    private String base64UrlEncode(String source) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(source.getBytes());
    }
}
