package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoMessageRecipientSnapshotState {

    private Long id;
    private String receiver;
    private String recvName;
    private String msgId;
    private String smid;
    private AligoMessageRecipientStatus recipientStatus;
    private String rslt;
    private String rsltMessage;
    private LocalDateTime sentAt;
    private LocalDateTime resultAt;
    private LocalDateTime reportAt;
}
