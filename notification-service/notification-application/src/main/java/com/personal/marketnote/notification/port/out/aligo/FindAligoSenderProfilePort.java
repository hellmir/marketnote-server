package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoSenderProfile;

import java.util.List;
import java.util.Optional;

public interface FindAligoSenderProfilePort {

    Optional<AligoSenderProfile> findBySenderKey(String senderKey);

    List<AligoSenderProfile> findAll();
}
