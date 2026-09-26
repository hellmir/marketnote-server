package com.personal.marketnote.file.adapter.in.client.file.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateFilesRequest {
    @ArraySchema(
            arraySchema = @Schema(
                    name = "file",
                    description = "업로드할 파일 목록",
                    requiredMode = Schema.RequiredMode.REQUIRED
            ),
            schema = @Schema(type = "string", format = "binary")
    )
    private List<MultipartFile> file;

    @ArraySchema(
            arraySchema = @Schema(
                    name = "sort",
                    description = "파일 종류 목록",
                    requiredMode = Schema.RequiredMode.REQUIRED
            ),
            schema = @Schema(type = "string", example = "PRODUCT_CATALOG_IMAGE")
    )
    private List<String> sort;

    @ArraySchema(
            arraySchema = @Schema(
                    name = "extension",
                    description = "파일 확장자 목록",
                    requiredMode = Schema.RequiredMode.NOT_REQUIRED
            ),
            schema = @Schema(type = "string", example = "jpg")
    )
    private List<String> extension;

    @ArraySchema(
            arraySchema = @Schema(
                    name = "name",
                    description = "파일명 목록",
                    requiredMode = Schema.RequiredMode.NOT_REQUIRED
            ),
            schema = @Schema(type = "string", example = "스프링노트1")
    )
    private List<String> name;

    @Schema(
            name = "ownerType",
            description = "소유자 유형",
            example = "PRODUCT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "소유자 유형은 필수값입니다.")
    private String ownerType;

    @Schema(
            name = "ownerId",
            description = "소유자 ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "소유자 ID는 필수값입니다.")
    @Min(value = 1, message = "소유자 ID는 1 이상이어야 합니다.")
    @Max(value = Long.MAX_VALUE, message = "소유자 ID는 정수형 최대값을 초과할 수 없습니다.")
    private Long ownerId;

    @Schema(
            name = "ownerKey",
            description = "소유자 키",
            example = "01HZXQ2K6E5X7F9Y3Z1M8N4Q0P",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "소유자 키는 필수값입니다.")
    @Size(max = 64, message = "소유자 키는 64자를 초과할 수 없습니다.")
    private String ownerKey;
}
