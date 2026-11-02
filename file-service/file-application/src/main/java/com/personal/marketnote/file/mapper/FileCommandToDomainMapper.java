package com.personal.marketnote.file.mapper;

import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.FileDomainCreateState;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;

import java.util.List;

public class FileCommandToDomainMapper {
    public static List<FileDomain> mapToDomain(UpdateFilesCommand updateFilesCommand) {
        OwnerType ownerType = OwnerType.from(updateFilesCommand.ownerType());

        return updateFilesCommand.fileInfo()
                .stream()
                .map(fileInfo -> FileDomain.from(
                        FileDomainCreateState.builder()
                                .ownerType(ownerType)
                                .ownerId(updateFilesCommand.ownerId())
                                .sort(FileSort.from(fileInfo.sort()))
                                .extension(fileInfo.extension())
                                .name(fileInfo.name())
                                .userId(updateFilesCommand.requesterId())
                                .ownerKey(updateFilesCommand.ownerKey())
                                .build()
                ))
                .toList();
    }
}
