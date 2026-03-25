package com.personal.marketnote.common.saga;

import com.personal.marketnote.common.utility.FormatValidator;

import java.util.function.Function;

public record SagaStepDefinition<T>(
        String stepName,
        Function<T, String> action,
        Function<T, String> compensation
) {
    public boolean hasCompensation() {
        return FormatValidator.hasValue(compensation);
    }
}
