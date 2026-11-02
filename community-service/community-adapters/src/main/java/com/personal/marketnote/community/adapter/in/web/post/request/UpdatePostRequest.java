package com.personal.marketnote.community.adapter.in.web.post.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePostRequest {
    @Schema(
            name = "title",
            description = "게시글 제목",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 1, max = 255, message = "게시글 제목은 1자 이상 255자 이하로 입력해야 합니다.")
    private String title;

    @Schema(
            name = "content",
            description = "게시글 내용",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "게시글 내용은 필수값입니다.")
    @Size(min = 1, max = 5000, message = "게시글 내용은 1자 이상 5,000자 이하로 입력해야 합니다.")
    private String content;

    @Schema(
            name = "isImportant",
            description = "중요 공지 여부 (NOTICE + ANNOUNCEMENT 게시글에만 유효, 미전송 시 기존 값 유지)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean isImportant;
}
