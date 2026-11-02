package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipient;
import com.personal.marketnote.notification.port.out.aligo.SendBrandTalkExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AligoBrandTalkExternalAdapter implements SendBrandTalkExternalPort {

    private static final String BRANDTALK_SEND_PATH = "/brandtalk/template/send/";
    private static final int MAX_MMS_IMAGES = 3;

    private final AligoHttpClient aligoHttpClient;

    @Override
    public BrandTalkSendResponse send(
            AligoBrandTalkMessage message, List<byte[]> failoverImages, List<String> failoverImageFileNames
    ) {
        MultiValueMap<String, Object> params = buildSendParams(message);

        if (message.hasFailover()) {
            addFailoverParams(params, message, failoverImages, failoverImageFileNames);
        }

        AligoBrandTalkSendApiResponse response =
                aligoHttpClient.postMultipart(BRANDTALK_SEND_PATH, params, AligoBrandTalkSendApiResponse.class);

        return new BrandTalkSendResponse(
                response.info().mid(),
                response.info().scnt(),
                response.info().fcnt(),
                response.info().type(),
                response.info().unitCost(),
                response.info().totalCost()
        );
    }

    private MultiValueMap<String, Object> buildSendParams(AligoBrandTalkMessage message) {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("senderkey", message.getSenderKey());
        params.add("tpl_code", message.getTemplateCode());
        params.add("sender", message.getSender());
        params.add("kakao_target", message.getKakaoTarget().name());
        params.add("advert_yn", message.isAdvertYn() ? "Y" : "N");

        List<AligoBrandTalkMessageRecipient> recipients = message.getRecipients();
        for (int i = 0; i < recipients.size(); i++) {
            AligoBrandTalkMessageRecipient recipient = recipients.get(i);
            int index = i + 1;
            params.add("receiver_" + index, recipient.getReceiver());
            params.add("receiver_" + index + "_message", recipient.getReceiverMessage());
        }

        return params;
    }

    private void addFailoverParams(
            MultiValueMap<String, Object> params, AligoBrandTalkMessage message,
            List<byte[]> failoverImages, List<String> failoverImageFileNames
    ) {
        params.add("failover", "Y");

        if (FormatValidator.hasValue(message.getFailoverSubject())) {
            params.add("failover_subject", message.getFailoverSubject());
        }

        if (FormatValidator.hasValue(message.getFailoverType())) {
            params.add("failover_type", message.getFailoverType().name());
        }

        if (FormatValidator.hasValue(message.getFailoverType()) && message.getFailoverType().isMms()) {
            addFailoverMmsImages(params, failoverImages, failoverImageFileNames);
        }
    }

    private void addFailoverMmsImages(
            MultiValueMap<String, Object> params,
            List<byte[]> failoverImages, List<String> failoverImageFileNames
    ) {
        if (FormatValidator.hasNoValue(failoverImages) || failoverImages.isEmpty()) {
            return;
        }

        int imageCount = Math.min(failoverImages.size(), MAX_MMS_IMAGES);
        for (int i = 0; i < imageCount; i++) {
            Resource resource = createResource(failoverImages.get(i), failoverImageFileNames.get(i));
            params.add("failover_mms" + (i + 1), resource);
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
