package com.personal.marketnote.user.port.in.usecase.remotearea;

import com.personal.marketnote.user.port.in.command.remotearea.RegisterRemoteAreaCommand;

public interface RegisterRemoteAreaUseCase {
    void registerRemoteArea(RegisterRemoteAreaCommand command);
}
