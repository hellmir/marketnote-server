package com.personal.marketnote.user.port.out.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;

import java.util.List;

public interface FindRemoteAreaPort {
    boolean existsByAddress(String province, String district, String village, String subarea);

    List<RemoteArea> findAllActive();
}
