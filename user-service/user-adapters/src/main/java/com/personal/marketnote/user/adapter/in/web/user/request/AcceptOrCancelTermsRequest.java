package com.personal.marketnote.user.adapter.in.web.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class AcceptOrCancelTermsRequest {
    @Schema(
            name = "ids",
            description = "동의 또는 철회할 약관 ID 목록",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "\"ids\" should not be empty. (+ not null)")
    private List<Long> ids;
}
