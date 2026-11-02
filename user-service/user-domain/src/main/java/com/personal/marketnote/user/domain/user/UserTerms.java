package com.personal.marketnote.user.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class UserTerms {
    private User user;
    private Terms terms;
    private Boolean agreementYn;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static UserTerms from(UserTermsCreateState state) {
        return UserTerms.builder()
                .user(state.getUser())
                .terms(state.getTerms())
                .agreementYn(state.getAgreementYn())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public static UserTerms from(UserTermsSnapshotState state) {
        return UserTerms.builder()
                .user(state.getUser())
                .terms(state.getTerms())
                .agreementYn(state.getAgreementYn())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public void acceptOrCancel() {
        agreementYn = !agreementYn;
    }

    public boolean isRequiredTermsAgreed() {
        return !terms.isRequired() || agreementYn;
    }

    public void disagree() {
        agreementYn = false;
    }
}
