package com.personal.marketnote.file.port.in.command;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record UpdateFileCommand(
        MultipartFile file,
        String sort,
        String extension,
        String name
) {
}
