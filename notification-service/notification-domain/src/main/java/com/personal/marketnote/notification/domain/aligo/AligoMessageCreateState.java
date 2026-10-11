package com.personal.marketnote.notification.domain.aligo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoMessageCreateState {

    private String senderKey;
    private String templateCode;
    private String sender;
    private AligoMessageType messageType;
    private String content;
    private String title;
    private String buttons;
    private LocalDateTime requestedAt;
    private List<AligoMessageRecipientCreateState> recipients;
}
