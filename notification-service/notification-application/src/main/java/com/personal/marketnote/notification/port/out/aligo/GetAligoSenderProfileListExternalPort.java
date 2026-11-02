package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;

import java.util.List;

public interface GetAligoSenderProfileListExternalPort {

    List<AligoSenderProfile> getProfileList();
}
