package com.personal.marketnote.file.adapter.in.client.file.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UpdateFileRequest {
    @Schema(
            name = "file",
            description = "파일",
            requiredMode = Schema.RequiredMode.REQUIRED,
            type = "string",
            format = "binary"
    )
    @NotNull(message = "파일은 필수값입니다.")
    private MultipartFile file;

    @Schema(
            name = "sort",
            description = "파일 종류",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "파일 종류는 필수값입니다.")
    private String sort;

    @Schema(
            name = "extension",
            description = "파일 확장자",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String extension;

    @Schema(
            name = "name",
            description = "파일명",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;
}
