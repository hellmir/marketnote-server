package com.personal.marketnote.commerce.port.out.returntracker;

import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;

public interface SaveReturnTrackerPort {
    ReturnTracker save(ReturnTracker returnTracker);
}
