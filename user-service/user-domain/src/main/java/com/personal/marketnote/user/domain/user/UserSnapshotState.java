package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.domain.authentication.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSnapshotState {
    private final Long id;
    private final UUID userKey;
    private final Nickname nickname;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phoneNumber;
    private final ReferenceCode referenceCode;
    private final ReferenceCode referredUserCode;
    private final Role role;
    private final List<UserAuthProvider> userAuthProviders;
    private final List<UserTerms> userTerms;
    private final LocalDateTime signedUpAt;
    private final LocalDateTime lastLoggedInAt;
    private final EntityStatus status;
    private final Boolean withdrawalYn;
    private final LocalDateTime withdrawnAt;
    private final Long orderNum;
    private final int penaltyCount;
    private final LocalDateTime deactivatedUntil;
}
