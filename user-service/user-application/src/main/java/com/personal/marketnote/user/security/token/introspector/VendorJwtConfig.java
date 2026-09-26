package com.personal.marketnote.user.security.token.introspector;

import java.util.List;

public record VendorJwtConfig(String jwksUri, List<String> issuers, String audience) {
}
