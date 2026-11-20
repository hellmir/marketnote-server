package com.personal.marketnote.fulfillment.utility;

public interface Sleeper {

    void sleepWithJitter(long maxMillis);

    void sleepWithBaseAndJitter(long baseMillis, long jitterMaxMillis);
}
