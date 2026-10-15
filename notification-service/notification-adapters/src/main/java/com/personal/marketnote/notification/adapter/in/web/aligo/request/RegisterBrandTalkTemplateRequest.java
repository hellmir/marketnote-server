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

        CouponRequest coupon,

        @Size(max = 20, message = "templateHeader는 최대 20자까지 입력할 수 있습니다")
        String templateHeader,

        WideItemRequest mainWideItem,

        @Size(min = 2, max = 3, message = "subWideItems는 최소 2개, 최대 3개입니다")
        List<WideItemRequest> subWideItems,

        @Size(max = 20, message = "carouselHeader는 최대 20자까지 입력할 수 있습니다")
        String carouselHeader,

        List<CarouselItemRequest> carouselItems,

        List<CarouselCommerceRequest> carouselCommerces,

        CarouselTailRequest carouselTail,

        String videoUrl,

        String videoThumbnailUrl,

        CommerceInfoRequest commerceInfo
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

    public record WideItemRequest(
            String title,
            String imageUrl,
            String linkM,
            String linkP,
            String linkI,
            String linkA
    ) {
    }

    public record CarouselItemRequest(
            String header,
            String content,
            List<ButtonRequest> buttons,
            CouponRequest coupon
    ) {
    }

    public record CarouselCommerceRequest(
            String title,
            String regularPrice,
            String discountPrice,
            String discountRate,
            String discountFixed
    ) {
    }

    public record CarouselTailRequest(
            String linkM
    ) {
    }

    public record CommerceInfoRequest(
            String title,
            String regularPrice,
            String discountPrice,
            String discountRate,
            String discountFixed
    ) {
    }
}
