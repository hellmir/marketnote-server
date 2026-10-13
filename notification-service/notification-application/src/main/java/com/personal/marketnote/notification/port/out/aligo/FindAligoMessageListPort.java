package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface FindAligoMessageListPort {

    List<AligoMessage> findMessages(LocalDateTime startDate, LocalDateTime endDate, int page, int limit);

    long countMessages(LocalDateTime startDate, LocalDateTime endDate);
}
