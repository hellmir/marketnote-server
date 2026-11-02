package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;

import java.util.Optional;

public interface FindBrandTalkMessagePort {

    Optional<AligoBrandTalkMessage> findById(Long id);
}
