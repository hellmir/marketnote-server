package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Terms {
    private Long id;
    private String content;
    private Boolean requiredYn;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private EntityStatus status;

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
