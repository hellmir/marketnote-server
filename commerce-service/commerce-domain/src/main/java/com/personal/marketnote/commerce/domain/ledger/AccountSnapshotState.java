package com.personal.marketnote.commerce.domain.ledger;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AccountSnapshotState {
    private Long id;
    private String name;
    private AccountType accountType;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
