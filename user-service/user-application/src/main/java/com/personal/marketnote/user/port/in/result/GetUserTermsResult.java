package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.UserTerms;

import java.util.List;
import java.util.stream.Collectors;

public record GetUserTermsResult(
        List<UpdateUserTermResult> userTerms
) {
    public static GetUserTermsResult from(List<UserTerms> userTerms) {
        return new GetUserTermsResult(
                userTerms.stream().map(UpdateUserTermResult::from)
                        .collect(Collectors.toList())
        );
    }
}
