package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoMessageRecipientCreateState {

    private String receiver;
    private String recvName;
}
