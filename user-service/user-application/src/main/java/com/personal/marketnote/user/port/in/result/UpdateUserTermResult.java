package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.UserTerms;

public record UpdateUserTermResult(
        Long id,
        String content,
        Boolean isRequired,
        Boolean isAgreed
) {
    public static UpdateUserTermResult from(UserTerms userTerms) {
        Terms terms = userTerms.getTerms();

        return new UpdateUserTermResult(
                terms.getId(),
                terms.getContent(),
                terms.getRequiredYn(),
                userTerms.getAgreementYn()
        );
    }
}
