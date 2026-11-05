package com.personal.marketnote.common.application.file.port.in.result;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record GetFileResult(
        Long id,
        String sort,
        String extension,
        String name,
        String storageUrl,
        List<String> resizedStorageUrls,
        Long orderNum
) {
    public static GetFileResult of(String imageUrl, List<String> resizedImageUrls) {
        return GetFileResult.builder()
                .storageUrl(imageUrl)
                .resizedStorageUrls(resizedImageUrls)
                .orderNum(0L)
                .build();
    }
}
