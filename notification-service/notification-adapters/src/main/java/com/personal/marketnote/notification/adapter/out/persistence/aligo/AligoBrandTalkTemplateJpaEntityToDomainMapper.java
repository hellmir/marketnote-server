package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkTemplateJpaEntity;
import com.personal.marketnote.notification.domain.aligo.*;

import java.util.List;

public class AligoBrandTalkTemplateJpaEntityToDomainMapper {

    private AligoBrandTalkTemplateJpaEntityToDomainMapper() {
    }

    public static AligoBrandTalkTemplate mapToDomain(AligoBrandTalkTemplateJpaEntity entity, ObjectMapper objectMapper) {
        List<BrandTalkButton> buttons = deserializeButtons(entity.getButtonsJson(), objectMapper);
        BrandTalkCoupon coupon = deserializeCoupon(entity.getCouponJson(), objectMapper);

        return AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateSnapshotState.builder()
                        .id(entity.getId())
                        .senderKey(entity.getSenderKey())
                        .templateCode(entity.getTemplateCode())
                        .templateName(entity.getTemplateName())
                        .templateType(entity.getTemplateType())
                        .templateContent(entity.getTemplateContent())
                        .adult(entity.isAdult())
                        .imageUrl(entity.getImageUrl())
                        .imageLink(entity.getImageLink())
                        .buttons(buttons)
                        .coupon(coupon)
                        .templateHeader(entity.getTemplateHeader())
                        .mainWideItem(deserializeWideItem(entity.getMainWideItemJson(), objectMapper))
                        .subWideItems(deserializeWideItems(entity.getSubWideItemsJson(), objectMapper))
                        .carouselHeader(entity.getCarouselHeader())
                        .carouselItems(deserializeCarouselItems(entity.getCarouselItemsJson(), objectMapper))
                        .carouselCommerces(deserializeCarouselCommerces(entity.getCarouselCommercesJson(), objectMapper))
                        .carouselTail(deserializeCarouselTail(entity.getCarouselTailJson(), objectMapper))
                        .videoUrl(entity.getVideoUrl())
                        .videoThumbnailUrl(entity.getVideoThumbnailUrl())
                        .commerceInfo(deserializeCommerceInfo(entity.getCommerceInfoJson(), objectMapper))
                        .templateStatus(entity.getTemplateStatus())
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }

    private static List<BrandTalkButton> deserializeButtons(String buttonsJson, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(buttonsJson)) {
            return List.of();
        }
        try {
            List<ButtonDto> dtos = objectMapper.readValue(buttonsJson, new TypeReference<>() {});
            return dtos.stream()
                    .map(dto -> BrandTalkButton.from(BrandTalkButtonCreateState.builder().name(dto.name()).linkType(AligoTemplateButtonLinkType.from(dto.linkType())).linkM(dto.linkM()).linkP(dto.linkP()).linkI(dto.linkI()).linkA(dto.linkA()).build()))
                    .toList();
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static BrandTalkCoupon deserializeCoupon(String couponJson, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(couponJson)) {
            return null;
        }
        try {
            CouponDto dto = objectMapper.readValue(couponJson, CouponDto.class);
            return BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title(dto.title()).description(dto.description()).linkM(dto.linkM()).linkP(dto.linkP()).linkI(dto.linkI()).linkA(dto.linkA()).build());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static BrandTalkWideItem deserializeWideItem(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            WideItemDto dto = objectMapper.readValue(json, WideItemDto.class);
            return BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title(dto.title()).imageUrl(dto.imageUrl()).linkM(dto.linkM()).linkP(dto.linkP()).linkI(dto.linkI()).linkA(dto.linkA()).build());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static List<BrandTalkWideItem> deserializeWideItems(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<WideItemDto> dtos = objectMapper.readValue(json, new TypeReference<>() {});
            return dtos.stream()
                    .map(dto -> BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title(dto.title()).imageUrl(dto.imageUrl()).linkM(dto.linkM()).linkP(dto.linkP()).linkI(dto.linkI()).linkA(dto.linkA()).build()))
                    .toList();
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static List<BrandTalkCarouselItem> deserializeCarouselItems(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<CarouselItemDto> dtos = objectMapper.readValue(json, new TypeReference<>() {});
            return dtos.stream()
                    .map(dto -> {
                        List<BrandTalkButton> itemButtons = FormatValidator.hasValue(dto.buttons())
                                ? dto.buttons().stream()
                                .map(b -> BrandTalkButton.from(BrandTalkButtonCreateState.builder().name(b.name()).linkType(AligoTemplateButtonLinkType.from(b.linkType())).linkM(b.linkM()).linkP(b.linkP()).linkI(b.linkI()).linkA(b.linkA()).build()))
                                .toList()
                                : List.of();
                        BrandTalkCoupon itemCoupon = FormatValidator.hasValue(dto.coupon())
                                ? BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title(dto.coupon().title()).description(dto.coupon().description()).linkM(dto.coupon().linkM()).linkP(dto.coupon().linkP()).linkI(dto.coupon().linkI()).linkA(dto.coupon().linkA()).build())
                                : null;
                        return BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header(dto.header()).content(dto.content()).imageUrl(dto.imageUrl()).buttons(itemButtons).coupon(itemCoupon).build());
                    })
                    .toList();
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static List<BrandTalkCarouselCommerce> deserializeCarouselCommerces(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<CarouselCommerceDto> dtos = objectMapper.readValue(json, new TypeReference<>() {});
            return dtos.stream()
                    .map(dto -> BrandTalkCarouselCommerce.from(BrandTalkCarouselCommerceCreateState.builder().title(dto.title()).regularPrice(dto.regularPrice()).discountPrice(dto.discountPrice()).discountRate(dto.discountRate()).discountFixed(dto.discountFixed()).build()))
                    .toList();
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private static BrandTalkCarouselTail deserializeCarouselTail(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            CarouselTailDto dto = objectMapper.readValue(json, CarouselTailDto.class);
            return BrandTalkCarouselTail.of(dto.linkM());
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private record ButtonDto(String name, String linkType, String linkM, String linkP, String linkI, String linkA) {}

    private record CouponDto(String title, String description, String linkM, String linkP, String linkI, String linkA) {}

    private record WideItemDto(String title, String imageUrl, String linkM, String linkP, String linkI, String linkA) {}

    private record CarouselItemDto(
            String header, String content, String imageUrl,
            List<ButtonDto> buttons, CouponDto coupon
    ) {}

    private record CarouselCommerceDto(
            String title, String regularPrice, String discountPrice,
            String discountRate, String discountFixed
    ) {}

    private record CarouselTailDto(String linkM) {}

    private record CommerceInfoDto(
            String title, String regularPrice, String discountPrice,
            String discountRate, String discountFixed
    ) {}

    private static BrandTalkCommerceInfo deserializeCommerceInfo(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            CommerceInfoDto dto = objectMapper.readValue(json, CommerceInfoDto.class);
            return BrandTalkCommerceInfo.from(BrandTalkCommerceInfoCreateState.builder().title(dto.title()).regularPrice(dto.regularPrice()).discountPrice(dto.discountPrice()).discountRate(dto.discountRate()).discountFixed(dto.discountFixed()).build());
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
