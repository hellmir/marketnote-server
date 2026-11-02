package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.UserAuthProvider;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;

public record AccountResult(
        AuthVendor oauth2VendorId,
        String oidcId
) {
    public static AccountResult from(UserAuthProvider userAuthProvider) {
        return new AccountResult(
                userAuthProvider.getAuthVendor(),
                userAuthProvider.getOidcId()
        );
    }
}
