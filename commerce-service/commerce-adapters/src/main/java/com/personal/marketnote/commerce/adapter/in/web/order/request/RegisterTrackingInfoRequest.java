package com.personal.marketnote.commerce.adapter.in.web.order.request;

import com.personal.marketnote.commerce.domain.order.CourierCompany;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterTrackingInfoRequest {
    @Schema(
            name = "courierCompany",
            description = "택배사",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "CJ_LOGISTICS"
    )
    @NotNull(message = "택배사는 필수값입니다.")
    private CourierCompany courierCompany;

    @Schema(
            name = "trackingNumber",
            description = "송장번호",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1234567890"
    )
    @NotBlank(message = "송장번호는 필수값입니다.")
    @Size(max = 63, message = "송장번호는 63자 이하여야 합니다.")
    private String trackingNumber;
}
