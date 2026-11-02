package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoTemplate;

import java.util.List;

public interface GetAligoTemplateListExternalPort {

    List<AligoTemplate> getTemplateList(String senderKey);
}
