package com.personal.marketnote.user.security.token.introspector;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.exception.InvalidVendorJwtConfigException;

import java.util.List;
import java.util.Set;

public record VendorJwtConfig(String jwksUri, List<String> issuers, Set<String> allowedAudiences) {
    public VendorJwtConfig {
        if (FormatValidator.hasNoValue(jwksUri)) {
            throw new InvalidVendorJwtConfigException("jwksUri는 필수입니다.");
        }
        if (FormatValidator.hasNoValue(issuers) || issuers.isEmpty()) {
            throw new InvalidVendorJwtConfigException("issuers는 최소 1개 이상 필요합니다.");
        }
        if (FormatValidator.hasNoValue(allowedAudiences) || allowedAudiences.isEmpty()) {
            throw new InvalidVendorJwtConfigException("allowedAudiences는 최소 1개 이상 필요합니다.");
        }
        issuers = List.copyOf(issuers);
        allowedAudiences = Set.copyOf(allowedAudiences);
    }
}
