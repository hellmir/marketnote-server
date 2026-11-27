package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkTemplateJpaEntity;
import com.personal.marketnote.notification.domain.aligo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AligoBrandTalkTemplateJpaEntityToDomainMapper {

    private static final Logger log = LoggerFactory.getLogger(AligoBrandTalkTemplateJpaEntityToDomainMapper.class);

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
            List<ButtonDto> dtos = objectMapper.readValue(buttonsJson, new TypeReference<>() {
            });
            return dtos.stream()
                .map(dto -> BrandTalkButton.from(mapToButtonSnapshotState(dto)))
                .toList();
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 버튼 역직렬화 실패, 빈 리스트 반환", e);
            return List.of();
        }
    }

    private static BrandTalkCoupon deserializeCoupon(String couponJson, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(couponJson)) {
            return null;
        }
        try {
            CouponDto dto = objectMapper.readValue(couponJson, CouponDto.class);
            return BrandTalkCoupon.from(mapToCouponSnapshotState(dto));
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 쿠폰 역직렬화 실패, null 반환", e);
            return null;
        }
    }

    private static BrandTalkButtonSnapshotState mapToButtonSnapshotState(ButtonDto dto) {
        return BrandTalkButtonSnapshotState.builder()
            .name(dto.name())
            .linkType(AligoTemplateButtonLinkType.from(dto.linkType()))
            .linkM(dto.linkM())
            .linkP(dto.linkP())
            .linkI(dto.linkI())
            .linkA(dto.linkA())
            .build();
    }

    private static BrandTalkCouponSnapshotState mapToCouponSnapshotState(CouponDto dto) {
        return BrandTalkCouponSnapshotState.builder()
            .title(dto.title())
            .description(dto.description())
            .linkM(dto.linkM())
            .linkP(dto.linkP())
            .linkI(dto.linkI())
            .linkA(dto.linkA())
            .build();
    }

    private static BrandTalkWideItem deserializeWideItem(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            WideItemDto dto = objectMapper.readValue(json, WideItemDto.class);
            return mapToWideItem(dto);
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 와이드 아이템 역직렬화 실패, null 반환", e);
            return null;
        }
    }

    private static List<BrandTalkWideItem> deserializeWideItems(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<WideItemDto> dtos = objectMapper.readValue(json, new TypeReference<>() {
            });
            return dtos.stream()
                .map(AligoBrandTalkTemplateJpaEntityToDomainMapper::mapToWideItem)
                .toList();
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 와이드 아이템 리스트 역직렬화 실패, 빈 리스트 반환", e);
            return List.of();
        }
    }

    private static BrandTalkWideItem mapToWideItem(WideItemDto dto) {
        return BrandTalkWideItem.from(
            BrandTalkWideItemSnapshotState.builder()
                .title(dto.title())
                .imageUrl(dto.imageUrl())
                .linkM(dto.linkM())
                .linkP(dto.linkP())
                .linkI(dto.linkI())
                .linkA(dto.linkA())
                .build()
        );
    }

    private static List<BrandTalkCarouselItem> deserializeCarouselItems(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<CarouselItemDto> dtos = objectMapper.readValue(json, new TypeReference<>() {
            });
            return dtos.stream()
                .map(AligoBrandTalkTemplateJpaEntityToDomainMapper::mapToCarouselItem)
                .toList();
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 캐러셀 아이템 역직렬화 실패, 빈 리스트 반환", e);
            return List.of();
        }
    }

    private static BrandTalkCarouselItem mapToCarouselItem(CarouselItemDto dto) {
        List<BrandTalkButtonSnapshotState> buttons = FormatValidator.hasValue(dto.buttons())
            ? dto.buttons().stream()
            .map(AligoBrandTalkTemplateJpaEntityToDomainMapper::mapToButtonSnapshotState)
            .toList()
            : List.of();
        BrandTalkCouponSnapshotState coupon = FormatValidator.hasValue(dto.coupon())
            ? mapToCouponSnapshotState(dto.coupon())
            : null;
        return BrandTalkCarouselItem.from(
            BrandTalkCarouselItemSnapshotState.builder()
                .header(dto.header())
                .content(dto.content())
                .imageUrl(dto.imageUrl())
                .buttons(buttons)
                .coupon(coupon)
                .build()
        );
    }

    private static List<BrandTalkCarouselCommerce> deserializeCarouselCommerces(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return List.of();
        }
        try {
            List<CarouselCommerceDto> dtos = objectMapper.readValue(json, new TypeReference<>() {
            });
            return dtos.stream()
                .map(AligoBrandTalkTemplateJpaEntityToDomainMapper::mapToCarouselCommerce)
                .toList();
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 캐러셀 커머스 역직렬화 실패, 빈 리스트 반환", e);
            return List.of();
        }
    }

    private static BrandTalkCarouselCommerce mapToCarouselCommerce(CarouselCommerceDto dto) {
        return BrandTalkCarouselCommerce.from(
            BrandTalkCarouselCommerceSnapshotState.builder()
                .title(dto.title())
                .regularPrice(dto.regularPrice())
                .discountPrice(dto.discountPrice())
                .discountRate(dto.discountRate())
                .discountFixed(dto.discountFixed())
                .build()
        );
    }

    private static BrandTalkCarouselTail deserializeCarouselTail(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            CarouselTailDto dto = objectMapper.readValue(json, CarouselTailDto.class);
            return BrandTalkCarouselTail.of(dto.linkM());
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 캐러셀 tail 역직렬화 실패, null 반환", e);
            return null;
        }
    }

    private record ButtonDto(String name, String linkType, String linkM, String linkP, String linkI, String linkA) {
    }

    private record CouponDto(String title, String description, String linkM, String linkP, String linkI, String linkA) {
    }

    private record WideItemDto(String title, String imageUrl, String linkM, String linkP, String linkI, String linkA) {
    }

    private record CarouselItemDto(
        String header, String content, String imageUrl,
        List<ButtonDto> buttons, CouponDto coupon
    ) {
    }

    private record CarouselCommerceDto(
        String title, String regularPrice, String discountPrice,
        String discountRate, String discountFixed
    ) {
    }

    private record CarouselTailDto(String linkM) {
    }

    private record CommerceInfoDto(
        String title, String regularPrice, String discountPrice,
        String discountRate, String discountFixed
    ) {
    }

    private static BrandTalkCommerceInfo deserializeCommerceInfo(String json, ObjectMapper objectMapper) {
        if (FormatValidator.hasNoValue(json)) {
            return null;
        }
        try {
            CommerceInfoDto dto = objectMapper.readValue(json, CommerceInfoDto.class);
            return BrandTalkCommerceInfo.from(
                BrandTalkCommerceInfoSnapshotState.builder()
                    .title(dto.title())
                    .regularPrice(dto.regularPrice())
                    .discountPrice(dto.discountPrice())
                    .discountRate(dto.discountRate())
                    .discountFixed(dto.discountFixed())
                    .build()
            );
        } catch (JsonProcessingException e) {
            log.warn("fallback applied: 브랜드메시지 커머스 정보 역직렬화 실패, null 반환", e);
            return null;
        }
    }
}
