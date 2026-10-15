package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetBrandTalkTemplateListResult;
import com.personal.marketnote.notification.port.out.aligo.GetBrandTalkTemplateListExternalPort;
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
public class AligoBrandTalkTemplateExternalAdapter
        implements RegisterBrandTalkTemplateExternalPort, GetBrandTalkTemplateListExternalPort {

    private final AligoHttpClient aligoHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public void registerTemplate(
            AligoBrandTalkTemplate template, byte[] imageFile, String imageFileName,
            List<byte[]> carouselItemImageFiles, List<String> carouselItemImageFileNames
    ) {
        MultiValueMap<String, Object> params = buildTemplateParams(template);

        if (template.isWideItemList()) {
            addWideItemListParams(params, template, imageFile, imageFileName);
        }

        if (template.isCarousel()) {
            addCarouselParams(params, template, imageFile, imageFileName, carouselItemImageFiles, carouselItemImageFileNames);
        }

        if (template.isPremiumVideo()) {
            addPremiumVideoParams(params, template);
        }

        if (template.getTemplateType().isCommerce()) {
            addCommerceParams(params, template);
        }

        if (template.requiresImage() && imageFile != null) {
            Resource resource = createResource(imageFile, imageFileName);
            params.add("image", resource);
        }

        aligoHttpClient.postMultipart("/brandtalk/template/create/", params, AligoApiResponse.class);
    }

    @Override
    public GetBrandTalkTemplateListResult.ExternalListResponse getTemplateList(
            String senderKey, int page, int pageSize
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", senderKey);
        params.add("page", String.valueOf(page));
        params.add("pagesize", String.valueOf(pageSize));

        AligoBrandTalkTemplateListApiResponse response =
                aligoHttpClient.post("/brandtalk/template/list/", params, AligoBrandTalkTemplateListApiResponse.class);

        GetBrandTalkTemplateListResult.PageInfo pageInfo = new GetBrandTalkTemplateListResult.PageInfo(
                response.info().currentPage(), response.info().pageSize(), response.info().total()
        );

        List<AligoBrandTalkTemplateListApiResponse.TemplateItem> items =
                FormatValidator.hasValue(response.list()) ? response.list() : List.of();

        List<GetBrandTalkTemplateListResult.ExternalTemplateItem> templates = items.stream()
                .map(item -> new GetBrandTalkTemplateListResult.ExternalTemplateItem(
                        item.tplCode(), item.tplName(), item.tplType(), item.tplStatus(),
                        FormatValidator.hasValue(item.sendVariable()) ? item.sendVariable() : List.of(),
                        item.createAt()
                ))
                .toList();

        return new GetBrandTalkTemplateListResult.ExternalListResponse(pageInfo, templates);
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

    private void addWideItemListParams(
            MultiValueMap<String, Object> params, AligoBrandTalkTemplate template,
            byte[] imageFile, String imageFileName
    ) {
        if (FormatValidator.hasValue(template.getTemplateHeader())) {
            params.add("template_header", template.getTemplateHeader());
        }
        if (template.getMainWideItem() != null) {
            params.add("main_wide_item", serializeWideItem(template.getMainWideItem()));
        }
        if (template.getSubWideItems() != null && !template.getSubWideItems().isEmpty()) {
            for (int i = 0; i < template.getSubWideItems().size(); i++) {
                params.add("sub_wide_item" + (i + 1), serializeWideItem(template.getSubWideItems().get(i)));
            }
        }
        if (imageFile != null) {
            Resource mainImageResource = createResource(imageFile, imageFileName);
            params.add("main_wide_item_image", mainImageResource);
        }
    }

    private String serializeWideItem(BrandTalkWideItem item) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("title", item.getTitle());
        if (FormatValidator.hasValue(item.getImageUrl())) {
            map.put("imageUrl", item.getImageUrl());
        }
        if (FormatValidator.hasValue(item.getLinkM())) {
            map.put("linkM", item.getLinkM());
        }
        if (FormatValidator.hasValue(item.getLinkP())) {
            map.put("linkP", item.getLinkP());
        }
        if (FormatValidator.hasValue(item.getLinkI())) {
            map.put("linkI", item.getLinkI());
        }
        if (FormatValidator.hasValue(item.getLinkA())) {
            map.put("linkA", item.getLinkA());
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("와이드 아이템 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private void addCarouselParams(
            MultiValueMap<String, Object> params, AligoBrandTalkTemplate template,
            byte[] headerImageFile, String headerImageFileName,
            List<byte[]> itemImageFiles, List<String> itemImageFileNames
    ) {
        if (FormatValidator.hasValue(template.getCarouselHeader())) {
            params.add("carousel_header", template.getCarouselHeader());
        }
        if (headerImageFile != null) {
            params.add("carousel_header_image", createResource(headerImageFile, headerImageFileName));
        }

        List<BrandTalkCarouselItem> items = template.getCarouselItems();
        for (int i = 0; i < items.size(); i++) {
            BrandTalkCarouselItem item = items.get(i);
            int index = i + 1;

            params.add("carousel_list" + index, serializeCarouselListItem(item));

            if (itemImageFiles != null && i < itemImageFiles.size() && itemImageFiles.get(i) != null) {
                params.add("carousel_list_image" + index, createResource(itemImageFiles.get(i), itemImageFileNames.get(i)));
            }

            if (item.getButtons() != null && !item.getButtons().isEmpty()) {
                params.add("carousel_list_button" + index, serializeButtons(item.getButtons()));
            }

            if (item.getCoupon() != null) {
                params.add("carousel_list_coupon" + index, serializeCoupon(item.getCoupon()));
            }
        }

        if (template.getTemplateType().isCarouselCommerce()) {
            List<BrandTalkCarouselCommerce> commerces = template.getCarouselCommerces();
            for (int i = 0; i < commerces.size(); i++) {
                params.add("carousel_list_commerce" + (i + 1), serializeCarouselCommerce(commerces.get(i)));
            }
        }

        if (template.getCarouselTail() != null) {
            params.add("carousel_tail", serializeCarouselTail(template.getCarouselTail()));
        }
    }

    private String serializeCarouselListItem(BrandTalkCarouselItem item) {
        Map<String, String> map = new LinkedHashMap<>();
        if (FormatValidator.hasValue(item.getHeader())) {
            map.put("header", item.getHeader());
        }
        if (FormatValidator.hasValue(item.getContent())) {
            map.put("content", item.getContent());
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("캐러셀 아이템 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private String serializeCarouselCommerce(BrandTalkCarouselCommerce commerce) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("title", commerce.getTitle());
        map.put("regularPrice", commerce.getRegularPrice());
        if (FormatValidator.hasValue(commerce.getDiscountPrice())) {
            map.put("discountPrice", commerce.getDiscountPrice());
        }
        if (FormatValidator.hasValue(commerce.getDiscountRate())) {
            map.put("discountRate", commerce.getDiscountRate());
        }
        if (FormatValidator.hasValue(commerce.getDiscountFixed())) {
            map.put("discountFixed", commerce.getDiscountFixed());
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("캐러셀 커머스 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private String serializeCarouselTail(BrandTalkCarouselTail tail) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("linkM", tail.getLinkM());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("캐러셀 tail 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private void addPremiumVideoParams(MultiValueMap<String, Object> params, AligoBrandTalkTemplate template) {
        if (FormatValidator.hasValue(template.getVideoUrl())) {
            params.add("template_video", template.getVideoUrl());
        }
        if (FormatValidator.hasValue(template.getVideoThumbnailUrl())) {
            params.add("template_video_thumb", template.getVideoThumbnailUrl());
        }
    }

    private void addCommerceParams(MultiValueMap<String, Object> params, AligoBrandTalkTemplate template) {
        if (template.getCommerceInfo() != null) {
            params.add("template_commerce", serializeCommerceInfoForApi(template.getCommerceInfo()));
        }
    }

    private String serializeCommerceInfoForApi(BrandTalkCommerceInfo commerceInfo) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("title", commerceInfo.getTitle());
        map.put("regularPrice", commerceInfo.getRegularPrice());
        if (FormatValidator.hasValue(commerceInfo.getDiscountPrice())) {
            map.put("discountPrice", commerceInfo.getDiscountPrice());
        }
        if (FormatValidator.hasValue(commerceInfo.getDiscountRate())) {
            map.put("discountRate", commerceInfo.getDiscountRate());
        }
        if (FormatValidator.hasValue(commerceInfo.getDiscountFixed())) {
            map.put("discountFixed", commerceInfo.getDiscountFixed());
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("커머스 정보 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private Resource createResource(byte[] data, String filename) {
        return new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
