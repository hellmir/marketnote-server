package com.personal.marketnote.reward.domain.point;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UserPointHistory {
    private Long id;
    private Long userId;
    // 적립(+)/차감(-) 부호를 amount의 부호로 표현하므로 Money(>=0) 대신 Long을 유지한다.
    private Long amount;
    private Boolean isReflected;
    private UserPointSourceType sourceType;
    private Long sourceId;
    private String reason;
    private LocalDateTime accumulatedAt;
    private LocalDateTime createdAt;

    public static UserPointHistory from(UserPointHistoryCreateState state) {
        return UserPointHistory.builder()
                .userId(state.getUserId())
                .amount(state.getAmount())
                .isReflected(state.getIsReflected())
                .sourceType(state.getSourceType())
                .sourceId(state.getSourceId())
                .reason(state.getReason())
                .accumulatedAt(state.getAccumulatedAt())
                .build();
    }

    public static UserPointHistory from(UserPointHistorySnapshotState state) {
        return UserPointHistory.builder()
                .id(state.getId())
                .userId(state.getUserId())
                .amount(state.getAmount())
                .isReflected(state.getIsReflected())
                .sourceType(state.getSourceType())
                .sourceId(state.getSourceId())
                .reason(state.getReason())
                .accumulatedAt(state.getAccumulatedAt())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
