package com.personal.marketnote.file.domain.file;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResizedFileCreateState {
    private final Long fileId;
    private final String size;
    private final String storageUrl;
}
