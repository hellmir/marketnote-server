package com.personal.marketnote.community.port.out.event;

public interface PublishPostEventPort {

    void publishNoticeRegisteredEvent(Long postId, String title);

    void publishEventRegisteredEvent(Long postId, String title);
}
