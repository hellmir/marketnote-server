package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;

public interface SaveBrandTalkTemplatePort {

    AligoBrandTalkTemplate save(AligoBrandTalkTemplate template);
}
