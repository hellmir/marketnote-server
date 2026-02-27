package com.personal.marketnote.common.utility;

import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import static com.personal.marketnote.common.domain.exception.ExceptionMessage.INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE;

public class ElementExtractor {
    public static Long extractUserId(OAuth2AuthenticatedPrincipal principal) {
        if (FormatValidator.hasNoValue(principal) || FormatValidator.equals(principal.getName(), "-1")) {
            throw new AuthenticationFailedException(INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE);
        }

        return FormatConverter.parseId(principal.getName());
    }

    private static final String DEFAULT_ROLE = "BUYER";

    /**
     * principal의 authorities에서 첫 번째 역할명을 추출한다.
     * ROLE_ 접두사를 제거하여 BUYER, SELLER, ADMIN 등으로 반환한다.
     * 역할을 찾지 못하면 가장 제한적인 역할인 BUYER를 기본값으로 반환하여,
     * 외부 요청이 서비스 내부 호출로 오인되는 것을 방지한다.
     *
     * @param principal 인증된 사용자 정보
     * @return 역할명 (BUYER, SELLER, ADMIN 등), 역할이 없으면 BUYER (기본값)
     */
    public static String extractRole(OAuth2AuthenticatedPrincipal principal) {
        if (FormatValidator.hasNoValue(principal) || FormatValidator.hasNoValue(principal.getAuthorities())) {
            return DEFAULT_ROLE;
        }

        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(FormatValidator::hasValue)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse(DEFAULT_ROLE);
    }
}
