package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.UserAuthProvider;

import java.util.List;
import java.util.stream.Collectors;

public record AccountInfoResult(
        List<AccountResult> accounts
) {
    public static AccountInfoResult from(List<UserAuthProvider> userAuthProviders) {
        return new AccountInfoResult(
                userAuthProviders.stream()
                        .map(AccountResult::from)
                        .collect(Collectors.toList())
        );
    }
}
