package com.personal.marketnote.fulfillment.utility;

public class NoOpSleeper implements Sleeper {

    @Override
    public void sleepWithJitter(long maxMillis) {
    }

    @Override
    public void sleepWithBaseAndJitter(long baseMillis, long jitterMaxMillis) {
    }
}
