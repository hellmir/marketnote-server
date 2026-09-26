package com.personal.marketnote.user.port.in.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.UserCreateState;
import com.personal.marketnote.user.port.in.command.SignUpCommand;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserCommandToStateMapper {
    public static UserCreateState mapToState(
            SignUpCommand command,
            AuthVendor authVendor,
            String oidcId,
            List<Terms> terms,
            String referenceCode,
            PasswordEncoder passwordEncoder
    ) {
        String encodedPassword = null;
        if (authVendor.isNative() && command.hasPassword()) {
            encodedPassword = passwordEncoder.encode(command.password());
        }

        String nickname = FormatValidator.hasValue(command.nickname()) ? command.nickname() : null;

        return UserCreateState.builder()
                .authVendor(authVendor)
                .oidcId(oidcId)
                .nickname(nickname)
                .email(command.email())
                .encodedPassword(encodedPassword)
                .fullName(command.fullName())
                .phoneNumber(command.phoneNumber())
                .terms(terms)
                .referenceCode(referenceCode)
                .build();
    }
}
