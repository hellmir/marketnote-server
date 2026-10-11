package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessage;

import java.util.Optional;

public interface FindAligoMessagePort {

    Optional<AligoMessage> findById(Long id);
}
