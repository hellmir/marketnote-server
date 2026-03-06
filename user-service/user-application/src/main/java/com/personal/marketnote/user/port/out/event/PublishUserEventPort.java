package com.personal.marketnote.user.port.out.event;

public interface PublishUserEventPort {

    void publishUserSignupCompletedEvent(Long userId, String userKey);

    void publishUserReferralCompletedEvent(Long requestUserId, Long referredUserId);
}
