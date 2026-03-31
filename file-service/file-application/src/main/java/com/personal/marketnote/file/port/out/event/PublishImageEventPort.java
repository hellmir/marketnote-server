package com.personal.marketnote.file.port.out.event;

import java.util.List;

public interface PublishImageEventPort {

    void publishImageCreatedEvents(List<ImageEventCommand> events);

    void publishImageDeletedEvents(List<ImageEventCommand> events);
}
