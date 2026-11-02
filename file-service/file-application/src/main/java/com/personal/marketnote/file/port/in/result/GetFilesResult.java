package com.personal.marketnote.file.port.in.result;

import com.personal.marketnote.file.domain.file.FileDomain;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;
import java.util.Map;

public record GetFilesResult(List<FileItem> files) {
    @Builder(access = AccessLevel.PRIVATE)
    public record FileItem(
            Long id,
            String sort,
            String extension,
            String name,
            String storageUrl,
            List<String> resizedStorageUrls,
            Long orderNum
    ) {
        public static GetFilesResult.FileItem from(FileDomain file, Map<Long, List<String>> fileIdToUrls) {
            return FileItem.builder()
                    .id(file.getId())
                    .sort(file.getSort().name())
                    .extension(file.getExtension())
                    .name(file.getName())
                    .storageUrl(file.getStorageUrl())
                    .resizedStorageUrls(fileIdToUrls.getOrDefault(file.getId(), List.of()))
                    .orderNum(file.getOrderNum())
                    .build();
        }
    }
}


