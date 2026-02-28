package com.personal.marketnote.commerce.domain.ledger;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Account {
    private Long id;
    private String name;
    private AccountType accountType;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Account from(AccountSnapshotState state) {
        return Account.builder()
                .id(state.getId())
                .name(state.getName())
                .accountType(state.getAccountType())
                .status(state.getStatus())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
    }

    public boolean isActive() {
        return this.status == EntityStatus.ACTIVE;
    }
}
