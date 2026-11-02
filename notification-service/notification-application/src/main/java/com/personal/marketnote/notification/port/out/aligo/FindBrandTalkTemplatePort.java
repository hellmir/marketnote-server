package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;

import java.util.Optional;

public interface FindBrandTalkTemplatePort {

    Optional<AligoBrandTalkTemplate> findByTemplateCode(String templateCode);
}
