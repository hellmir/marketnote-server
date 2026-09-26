package com.personal.marketnote.file.adapter.in.client.file.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.adapter.in.client.file.request.UpdateFilesRequest;
import com.personal.marketnote.file.port.in.command.UpdateFileCommand;
import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class FileRequestToCommandMapper {
    public static UpdateFilesCommand mapToCommand(UpdateFilesRequest updateFilesRequest, Long requesterId) {
        List<MultipartFile> files = updateFilesRequest.getFile();
        List<String> sorts = updateFilesRequest.getSort();
        List<String> extensions = updateFilesRequest.getExtension();
        List<String> names = updateFilesRequest.getName();

        if (FormatValidator.hasNoValue(files)) {
            return UpdateFilesCommand.builder()
                    .fileInfo(new ArrayList<>(0))
                    .ownerType(updateFilesRequest.getOwnerType())
                    .ownerId(updateFilesRequest.getOwnerId())
                    .ownerKey(updateFilesRequest.getOwnerKey())
                    .requesterId(requesterId)
                    .build();
        }

        int fileSize = files.size();
        List<UpdateFileCommand> fileInfo = new ArrayList<>(fileSize);
        for (int i = 0; i < fileSize; i++) {
            fileInfo.add(UpdateFileCommand.builder()
                    .file(files.get(i))
                    .sort(getOrNull(sorts, i))
                    .extension(getOrNull(extensions, i))
                    .name(getOrNull(names, i))
                    .build());
        }

        return UpdateFilesCommand.builder()
                .fileInfo(fileInfo)
                .ownerType(updateFilesRequest.getOwnerType())
                .ownerId(updateFilesRequest.getOwnerId())
                .ownerKey(updateFilesRequest.getOwnerKey())
                .requesterId(requesterId)
                .build();
    }

    private static String getOrNull(List<String> values, int index) {
        if (FormatValidator.hasNoValue(values) || index >= values.size()) {
            return null;
        }

        return values.get(index);
    }
}
