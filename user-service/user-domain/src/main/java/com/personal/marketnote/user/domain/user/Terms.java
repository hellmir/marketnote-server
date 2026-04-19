package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Terms {
    private final Long id;
    private final String content;
    private final Boolean requiredYn;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final EntityStatus status;

    public static Terms from(TermsCreateState state) {
        return Terms.builder()
                .content(state.getContent())
                .requiredYn(state.getRequiredYn())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .status(state.getStatus())
                .build();
    }

    public static Terms from(TermsSnapshotState state) {
        return Terms.builder()
                .id(state.getId())
                .content(state.getContent())
                .requiredYn(state.getRequiredYn())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .status(state.getStatus())
                .build();
    }

    public boolean isRequired() {
        return requiredYn;
    }
}
