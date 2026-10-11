package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;

public interface SaveAligoSenderProfilePort {

    AligoSenderProfile save(AligoSenderProfile senderProfile);
}
