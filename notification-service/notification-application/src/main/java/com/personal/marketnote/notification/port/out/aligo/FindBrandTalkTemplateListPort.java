package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;

import java.util.List;

public interface FindBrandTalkTemplateListPort {

    List<AligoBrandTalkTemplate> findAllBySenderKey(String senderKey);
}
