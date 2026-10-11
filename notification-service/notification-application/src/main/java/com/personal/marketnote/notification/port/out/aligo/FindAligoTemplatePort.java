package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoTemplate;

import java.util.Optional;

public interface FindAligoTemplatePort {

    Optional<AligoTemplate> findByTemplateCode(String templateCode);
}
