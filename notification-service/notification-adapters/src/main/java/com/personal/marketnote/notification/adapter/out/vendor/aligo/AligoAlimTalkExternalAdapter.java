package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoApiRequestFailedException;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipient;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AligoAlimTalkExternalAdapter implements SendAligoAlimTalkExternalPort {

    private static final String ALIMTALK_SEND_PATH = "/akv10/alimtalk/send/";

    private final AligoHttpClient aligoHttpClient;

    @Override
    public AligoAlimTalkSendResponse send(AligoMessage message) {
        MultiValueMap<String, String> params = buildSendParams(message);

        AligoAlimTalkApiResponse response = aligoHttpClient.post(
                ALIMTALK_SEND_PATH, params, AligoAlimTalkApiResponse.class
        );

        if (FormatValidator.hasNoValue(response.info())) {
            throw new AligoApiRequestFailedException(
                    "알리고 알림톡 발송 응답에 info가 없습니다. resultCode=" + response.resultCode()
            );
        }

        return new AligoAlimTalkSendResponse(
                response.info().mid(),
                response.info().scnt(),
                response.info().fcnt(),
                response.info().smid()
        );
    }

    private MultiValueMap<String, String> buildSendParams(AligoMessage message) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("senderkey", message.getSenderKey());
        params.add("tpl_code", message.getTemplateCode());
        params.add("sender", message.getSender());

        if (message.hasFailoverContent()) {
            params.add("failover", "Y");
        }

        List<AligoMessageRecipient> recipients = message.getRecipients();
        for (int i = 0; i < recipients.size(); i++) {
            int index = i + 1;
            AligoMessageRecipient recipient = recipients.get(i);
            params.add("receiver_" + index, recipient.getReceiver());
            if (FormatValidator.hasValue(recipient.getRecvName())) {
                params.add("recvname_" + index, recipient.getRecvName());
            }
            params.add("subject_" + index, message.getTitle());
            params.add("message_" + index, message.getContent());
            if (FormatValidator.hasValue(message.getButtons())) {
                params.add("button_" + index, message.getButtons());
            }
            if (message.hasFailoverContent()) {
                if (FormatValidator.hasValue(message.getFailoverSubject())) {
                    params.add("fsubject_" + index, message.getFailoverSubject());
                }
                if (FormatValidator.hasValue(message.getFailoverMessageBody())) {
                    params.add("fmessage_" + index, message.getFailoverMessageBody());
                }
            }
        }

        return params;
    }
}
