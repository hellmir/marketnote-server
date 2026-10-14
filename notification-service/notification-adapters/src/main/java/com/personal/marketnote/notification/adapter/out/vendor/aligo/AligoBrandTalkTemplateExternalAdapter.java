package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.RegisterBrandTalkTemplateExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AligoBrandTalkTemplateExternalAdapter implements RegisterBrandTalkTemplateExternalPort {

    private final AligoHttpClient aligoHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public void registerTemplate(AligoBrandTalkTemplate template, byte[] imageFile, String imageFileName) {
        MultiValueMap<String, Object> params = buildTemplateParams(template);

        if (template.requiresImage() && imageFile != null) {
            Resource resource = new ByteArrayResource(imageFile) {
                @Override
                public String getFilename() {
                    return imageFileName;
                }
            };
            params.add("image", resource);
        }

        aligoHttpClient.postMultipart("/brandtalk/template/create/", params, AligoApiResponse.class);
    }

    private MultiValueMap<String, Object> buildTemplateParams(AligoBrandTalkTemplate template) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("senderkey", template.getSenderKey());
        params.add("tpl_code", template.getTemplateCode());
        params.add("tpl_name", template.getTemplateName());
        params.add("tpl_type", template.getTemplateType().name());
        params.add("tpl_content", template.getTemplateContent());
        params.add("adult", template.isAdult() ? "Y" : "N");

        if (FormatValidator.hasValue(template.getImageLink())) {
            params.add("image_link", template.getImageLink());
        }

        if (template.getButtons() != null && !template.getButtons().isEmpty()) {
            params.add("tpl_button", serializeButtons(template.getButtons()));
        }

        if (template.getCoupon() != null) {
            params.add("tpl_coupon", serializeCoupon(template.getCoupon()));
        }

        return params;
    }

    private String serializeButtons(List<BrandTalkButton> buttons) {
        List<Map<String, String>> buttonList = buttons.stream()
                .map(this::buttonToMap)
                .toList();

        try {
            return objectMapper.writeValueAsString(Map.of("button", buttonList));
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("브랜드메시지 버튼 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private String serializeCoupon(BrandTalkCoupon coupon) {
        Map<String, String> couponMap = new LinkedHashMap<>();
        couponMap.put("title", coupon.getTitle());
        if (FormatValidator.hasValue(coupon.getDescription())) {
            couponMap.put("description", coupon.getDescription());
        }
        if (FormatValidator.hasValue(coupon.getLinkM())) {
            couponMap.put("linkM", coupon.getLinkM());
        }
        if (FormatValidator.hasValue(coupon.getLinkP())) {
            couponMap.put("linkP", coupon.getLinkP());
        }
        if (FormatValidator.hasValue(coupon.getLinkI())) {
            couponMap.put("linkI", coupon.getLinkI());
        }
        if (FormatValidator.hasValue(coupon.getLinkA())) {
            couponMap.put("linkA", coupon.getLinkA());
        }

        try {
            return objectMapper.writeValueAsString(couponMap);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("브랜드메시지 쿠폰 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, String> buttonToMap(BrandTalkButton button) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", button.getName());
        map.put("linkType", button.getLinkType().name());
        if (FormatValidator.hasValue(button.getLinkM())) {
            map.put("linkM", button.getLinkM());
        }
        if (FormatValidator.hasValue(button.getLinkP())) {
            map.put("linkP", button.getLinkP());
        }
        if (FormatValidator.hasValue(button.getLinkI())) {
            map.put("linkI", button.getLinkI());
        }
        if (FormatValidator.hasValue(button.getLinkA())) {
            map.put("linkA", button.getLinkA());
        }
        return map;
    }
}
