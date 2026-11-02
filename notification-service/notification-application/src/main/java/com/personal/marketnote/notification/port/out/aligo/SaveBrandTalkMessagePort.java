package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;

public interface SaveBrandTalkMessagePort {

    AligoBrandTalkMessage save(AligoBrandTalkMessage message);
}
