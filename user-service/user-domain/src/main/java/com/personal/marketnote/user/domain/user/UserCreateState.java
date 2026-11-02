package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserCreateState {
    private final AuthVendor authVendor;
    private final String oidcId;
    private final Nickname nickname;
    private final String email;
    private final String encodedPassword;
    private final String fullName;
    private final String phoneNumber;
    private final List<Terms> terms;
    private final ReferenceCode referenceCode;
    private final boolean guest;

    public boolean hasPassword() {
        return FormatValidator.hasValue(encodedPassword);
    }
}

