package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoTemplateListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoTemplateInspectionExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplateExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AligoTemplateExternalAdapter
        implements RegisterAligoTemplateExternalPort, UpdateAligoTemplateExternalPort,
        DeleteAligoTemplateExternalPort, GetAligoTemplateListExternalPort,
        RequestAligoTemplateInspectionExternalPort {

    private final AligoHttpClient aligoHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public void registerTemplate(AligoTemplate template) {
        MultiValueMap<String, String> params = buildTemplateParams(template);
        aligoHttpClient.post("/akv10/template/add/", params, AligoApiResponse.class);
    }

    @Override
    public void updateTemplate(AligoTemplate template) {
        MultiValueMap<String, String> params = buildTemplateParams(template);
        aligoHttpClient.post("/akv10/template/modify/", params, AligoApiResponse.class);
    }

    @Override
    public List<AligoTemplate> getTemplateList(String senderKey) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", senderKey);

        AligoTemplateListApiResponse response = aligoHttpClient.post(
                "/akv10/template/list/", params, AligoTemplateListApiResponse.class
        );

        if (response.list() == null) {
            return List.of();
        }

        return response.list().stream()
                .map(this::toTemplateDomain)
                .toList();
    }

    @Override
    public void requestInspection(String senderKey, String templateCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", senderKey);
        params.add("tpl_code", templateCode);
        aligoHttpClient.post("/akv10/template/request/", params, AligoApiResponse.class);
    }

    @Override
    public void deleteTemplate(String senderKey, String templateCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", senderKey);
        params.add("tpl_code", templateCode);
        aligoHttpClient.post("/akv10/template/del/", params, AligoApiResponse.class);
    }

    private MultiValueMap<String, String> buildTemplateParams(AligoTemplate template) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", template.getSenderKey());
        params.add("tpl_code", template.getTemplateCode());
        params.add("tpl_name", template.getTemplateName());
        params.add("tpl_content", template.getTemplateContent());

        if (template.getTemplateTitle() != null) {
            params.add("tpl_title", template.getTemplateTitle());
        }
        if (template.getTemplateSubtitle() != null) {
            params.add("tpl_subtitle", template.getTemplateSubtitle());
        }

        params.add("template_type", template.getTemplateType().name());
        params.add("template_em_type", template.getTemplateEmType().name());
        params.add("security_yn", template.isTemplateSecure() ? "Y" : "N");
        params.add("tpl_advert", template.isTemplateAdvert() ? "Y" : "N");

        if (template.getImageUrl() != null) {
            params.add("image_url", template.getImageUrl());
        }
        if (template.getImageName() != null) {
            params.add("image_name", template.getImageName());
        }

        if (template.getButtons() != null && !template.getButtons().isEmpty()) {
            params.add("tpl_button", serializeButtons(template.getButtons()));
        }

        return params;
    }

    private String serializeButtons(List<AligoTemplateButton> buttons) {
        List<Map<String, String>> buttonList = buttons.stream()
                .map(this::buttonToMap)
                .toList();

        try {
            return objectMapper.writeValueAsString(Map.of("button", buttonList));
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("버튼 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private AligoTemplate toTemplateDomain(AligoTemplateListApiResponse.TemplateItem item) {
        return AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .senderKey(item.senderkey())
                        .templateCode(item.templtCode())
                        .templateName(item.templtName())
                        .templateContent(item.templtContent() != null ? item.templtContent() : "")
                        .templateType(AligoTemplateType.from(item.templateType()))
                        .templateEmType(AligoTemplateEmType.from(item.templateEmphasizeType() != null ? item.templateEmphasizeType() : "NONE"))
                        .inspStatus(item.inspStatus())
                        .templateStatus(AligoTemplateStatus.from(item.status()))
                        .comments(item.comments())
                        .buttons(List.of())
                        .build()
        );
    }

    private Map<String, String> buttonToMap(AligoTemplateButton button) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ordering", String.valueOf(button.getOrdering()));
        map.put("name", button.getName());
        map.put("linkType", button.getLinkType().name());
        if (button.getLinkMo() != null) {
            map.put("linkMo", button.getLinkMo());
        }
        if (button.getLinkPc() != null) {
            map.put("linkPc", button.getLinkPc());
        }
        if (button.getLinkIos() != null) {
            map.put("linkIos", button.getLinkIos());
        }
        if (button.getLinkAnd() != null) {
            map.put("linkAnd", button.getLinkAnd());
        }
        return map;
    }
}
