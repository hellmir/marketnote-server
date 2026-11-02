package com.personal.marketnote.commerce.domain.returntracker;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReturnTrackerCreateState {
    private Long orderId;
    private String returnSlipNumber;
}
