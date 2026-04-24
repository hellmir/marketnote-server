package com.personal.marketnote.user.adapter.in.web.remotearea.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterRemoteAreaRequest {

    @Schema(name = "province", description = "광역시도 (최대 50자)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "광역시도는 필수값입니다.")
    @Size(max = 50, message = "광역시도는 최대 50자까지 입력할 수 있습니다.")
    private String province;

    @Schema(name = "district", description = "시군구 (최대 50자)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "시군구는 최대 50자까지 입력할 수 있습니다.")
    private String district;

    @Schema(name = "village", description = "읍면동 (최대 50자)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "읍면동은 최대 50자까지 입력할 수 있습니다.")
    private String village;

    @Schema(name = "subarea", description = "세부지역 (최대 50자)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 50, message = "세부지역은 최대 50자까지 입력할 수 있습니다.")
    private String subarea;
}
