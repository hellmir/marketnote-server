package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface FindPendingAligoMessagesPort {

    List<AligoMessage> findPendingMessages(LocalDateTime after);
}
