package com.personal.marketnote.common.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        MdcContextSnapshot snapshot = MdcContextSnapshot.capture();
        return () -> {
            try {
                snapshot.applyOrInitialize();
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
