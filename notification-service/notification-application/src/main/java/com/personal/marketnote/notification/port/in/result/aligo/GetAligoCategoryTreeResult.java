package com.personal.marketnote.notification.port.in.result.aligo;

import java.util.List;

public record GetAligoCategoryTreeResult(
        List<CategoryResult> categories
) {

    public record CategoryResult(
            String code,
            String name,
            List<CategoryResult> children
    ) {
    }
}
