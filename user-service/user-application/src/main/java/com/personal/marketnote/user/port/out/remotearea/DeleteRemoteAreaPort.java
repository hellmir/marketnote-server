package com.personal.marketnote.user.port.out.remotearea;

import com.personal.marketnote.user.domain.remotearea.RemoteArea;

public interface DeleteRemoteAreaPort {
    void deactivate(RemoteArea remoteArea);
}
