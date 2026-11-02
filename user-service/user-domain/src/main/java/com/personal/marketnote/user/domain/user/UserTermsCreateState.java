package com.personal.marketnote.user.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTermsCreateState {
    private final User user;
    private final Terms terms;
    private final Boolean agreementYn;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}

