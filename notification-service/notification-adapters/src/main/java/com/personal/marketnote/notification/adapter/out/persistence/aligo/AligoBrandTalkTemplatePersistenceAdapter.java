package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkTemplateJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoBrandTalkTemplateJpaRepository;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.FindBrandTalkTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkTemplatePort;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AligoBrandTalkTemplatePersistenceAdapter implements SaveBrandTalkTemplatePort, FindBrandTalkTemplatePort {

    private final AligoBrandTalkTemplateJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public AligoBrandTalkTemplate save(AligoBrandTalkTemplate template) {
        String buttonsJson = serializeButtons(template.getButtons());
        String couponJson = serializeCoupon(template.getCoupon());

        AligoBrandTalkTemplateJpaEntity entity = AligoBrandTalkTemplateJpaEntity.create(
                template.getSenderKey(), template.getTemplateCode(), template.getTemplateName(),
                template.getTemplateType(), template.getTemplateContent(),
                template.isAdult(), template.getImageUrl(), template.getImageLink(),
                buttonsJson, couponJson
        );

        AligoBrandTalkTemplateJpaEntity saved = repository.save(entity);
        return AligoBrandTalkTemplateJpaEntityToDomainMapper.mapToDomain(saved, objectMapper);
    }

    @Override
    public Optional<AligoBrandTalkTemplate> findByTemplateCode(String templateCode) {
        return repository.findByTemplateCode(templateCode)
                .map(entity -> AligoBrandTalkTemplateJpaEntityToDomainMapper.mapToDomain(entity, objectMapper));
    }

    private String serializeButtons(List<BrandTalkButton> buttons) {
        if (buttons == null || buttons.isEmpty()) {
            return null;
        }
        List<Map<String, String>> buttonList = buttons.stream()
                .map(this::buttonToMap)
                .toList();
        try {
            return objectMapper.writeValueAsString(buttonList);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String serializeCoupon(BrandTalkCoupon coupon) {
        if (coupon == null) {
            return null;
        }
        Map<String, String> couponMap = new LinkedHashMap<>();
        couponMap.put("title", coupon.getTitle());
        couponMap.put("description", coupon.getDescription());
        couponMap.put("linkM", coupon.getLinkM());
        couponMap.put("linkP", coupon.getLinkP());
        couponMap.put("linkI", coupon.getLinkI());
        couponMap.put("linkA", coupon.getLinkA());
        try {
            return objectMapper.writeValueAsString(couponMap);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, String> buttonToMap(BrandTalkButton button) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", button.getName());
        map.put("linkType", button.getLinkType().name());
        map.put("linkM", button.getLinkM());
        map.put("linkP", button.getLinkP());
        map.put("linkI", button.getLinkI());
        map.put("linkA", button.getLinkA());
        return map;
    }
}
