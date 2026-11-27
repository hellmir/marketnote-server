package com.personal.marketnote.fulfillment.utility;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class DefaultSleeper implements Sleeper {

    @Override
    public void sleepWithJitter(long maxMillis) {
        try {
            long jitteredSleepMillis = ThreadLocalRandom.current()
                .nextLong(Math.max(1L, maxMillis) + 1);
            Thread.sleep(jitteredSleepMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sleepWithBaseAndJitter(long baseMillis, long jitterMaxMillis) {
        try {
            long jitter = jitterMaxMillis <= 0 ? 0L : ThreadLocalRandom.current().nextLong(jitterMaxMillis);
            Thread.sleep(baseMillis + jitter);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
