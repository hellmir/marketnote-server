package com.personal.marketnote.community.port.out.event;

public interface PublishPostEventPort {

    void publishNoticeRegisteredEvent(Long postId, String title, boolean isImportant);

    void publishEventRegisteredEvent(Long postId, String title);

    void publishInquiryAnsweredEvent(Long userId, Long postId, String title, String board);
}
