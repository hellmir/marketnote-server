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
                    .map(dto -> BrandTalkButton.of(
                            dto.name(), AligoTemplateButtonLinkType.from(dto.linkType()),
                            dto.linkM(), dto.linkP(), dto.linkI(), dto.linkA()
                    ))
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
            return BrandTalkCoupon.of(
                    dto.title(), dto.description(),
                    dto.linkM(), dto.linkP(), dto.linkI(), dto.linkA()
            );
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private record ButtonDto(String name, String linkType, String linkM, String linkP, String linkI, String linkA) {}

    private record CouponDto(String title, String description, String linkM, String linkP, String linkI, String linkA) {}
}
