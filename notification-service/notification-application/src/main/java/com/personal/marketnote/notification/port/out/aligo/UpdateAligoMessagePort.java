package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessage;

public interface UpdateAligoMessagePort {

    void update(AligoMessage message);
}
