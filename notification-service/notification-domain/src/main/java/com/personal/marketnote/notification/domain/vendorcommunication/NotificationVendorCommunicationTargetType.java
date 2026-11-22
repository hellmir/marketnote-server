package com.personal.marketnote.notification.domain.vendorcommunication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationVendorCommunicationTargetType {
    PUSH_NOTIFICATION("푸시 알림"),
    ALIMTALK("알림톡"),
    FRIEND_TALK("친구톡"),
    ALIMTALK_TEMPLATE("알림톡 템플릿"),
    FRIEND_TALK_TEMPLATE("친구톡 템플릿"),
    SENDER_PROFILE("발신 프로필"),
    BALANCE("잔액/포인트"),
    CANCEL("발송 취소"),
    SEND_HISTORY("발송 이력");

    private final String description;
}
