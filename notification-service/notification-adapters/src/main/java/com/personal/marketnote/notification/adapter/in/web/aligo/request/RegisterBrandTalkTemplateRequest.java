package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterBrandTalkTemplateRequest(
        @NotBlank(message = "senderKey는 필수입니다")
        String senderKey,

        @NotBlank(message = "templateCode는 필수입니다")
        String templateCode,

        @NotBlank(message = "templateName은 필수입니다")
        String templateName,

        @NotBlank(message = "templateType은 필수입니다")
        String templateType,

        @NotBlank(message = "templateContent는 필수입니다")
        String templateContent,

        boolean adult,

        String imageLink,

        @Size(max = 5, message = "버튼은 최대 5개까지 등록할 수 있습니다")
        List<ButtonRequest> buttons,

        CouponRequest coupon
) {
    public record ButtonRequest(
            String name,
            String linkType,
            String linkM,
            String linkP,
            String linkI,
            String linkA
    ) {
    }

    public record CouponRequest(
            String title,
            String description,
            String linkM,
            String linkP,
            String linkI,
            String linkA
    ) {
    }
}
