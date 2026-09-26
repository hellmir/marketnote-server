package com.personal.marketnote.file.port.in.command;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateFilesCommand(
        List<UpdateFileCommand> fileInfo,
        String ownerType,
        Long ownerId,
        String ownerKey,
        Long requesterId
) {
}
