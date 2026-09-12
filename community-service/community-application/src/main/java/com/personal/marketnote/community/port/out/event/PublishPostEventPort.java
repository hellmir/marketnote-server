package com.personal.marketnote.community.port.out.event;

public interface PublishPostEventPort {

    void publishNoticeRegisteredEvent(Long postId, String title);
}
