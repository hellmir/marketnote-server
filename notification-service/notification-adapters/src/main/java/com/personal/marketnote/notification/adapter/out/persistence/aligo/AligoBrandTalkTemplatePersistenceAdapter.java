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
        String mainWideItemJson = serializeWideItem(template.getMainWideItem());
        String subWideItemsJson = serializeWideItems(template.getSubWideItems());

        AligoBrandTalkTemplateJpaEntity entity = AligoBrandTalkTemplateJpaEntity.create(
                template.getSenderKey(), template.getTemplateCode(), template.getTemplateName(),
                template.getTemplateType(), template.getTemplateContent(),
                template.isAdult(), template.getImageUrl(), template.getImageLink(),
                buttonsJson, couponJson,
                template.getTemplateHeader(), mainWideItemJson, subWideItemsJson
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

    private String serializeWideItem(BrandTalkWideItem item) {
        if (item == null) {
            return null;
        }
        Map<String, String> map = wideItemToMap(item);
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String serializeWideItems(List<BrandTalkWideItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<Map<String, String>> list = items.stream().map(this::wideItemToMap).toList();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Map<String, String> wideItemToMap(BrandTalkWideItem item) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("title", item.getTitle());
        map.put("imageUrl", item.getImageUrl());
        map.put("linkM", item.getLinkM());
        map.put("linkP", item.getLinkP());
        map.put("linkI", item.getLinkI());
        map.put("linkA", item.getLinkA());
        return map;
    }
}
