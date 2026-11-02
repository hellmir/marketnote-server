package com.personal.marketnote.common.utility;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

public class AuthorityValidator {
    public static boolean hasAdminRole(OAuth2AuthenticatedPrincipal principal) {
        return FormatValidator.hasValue(principal) && principal.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> Role.isAdmin(grantedAuthority.getAuthority()));
    }

    public static boolean hasSellerRole(OAuth2AuthenticatedPrincipal principal) {
        return FormatValidator.hasValue(principal) && principal.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> Role.isSeller(grantedAuthority.getAuthority()));
    }
}
