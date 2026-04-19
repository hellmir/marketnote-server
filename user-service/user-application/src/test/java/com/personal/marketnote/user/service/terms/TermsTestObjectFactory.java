package com.personal.marketnote.user.service.terms;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.user.*;

import java.time.LocalDateTime;

class TermsTestObjectFactory {
    static Terms createTerms(
            Long id,
            String content,
            boolean requiredYn,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            EntityStatus status
    ) {
        return Terms.from(TermsSnapshotState.builder()
                .id(id)
                .content(content)
                .requiredYn(requiredYn)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .status(status)
                .build());
    }

    static UserTerms createUserTerms(
            User user,
            Terms terms,
            boolean agreementYn,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        return UserTerms.from(UserTermsSnapshotState.builder()
                .user(user)
                .terms(terms)
                .agreementYn(agreementYn)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build());
    }
}
