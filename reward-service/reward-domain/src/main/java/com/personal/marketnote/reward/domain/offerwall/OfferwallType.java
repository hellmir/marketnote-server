package com.personal.marketnote.reward.domain.offerwall;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OfferwallType {
    ADPOPCORN("애드팝콘"),
    TNK("TNK"),
    ADISCOPE("애디스콥");

    private final String description;
}
