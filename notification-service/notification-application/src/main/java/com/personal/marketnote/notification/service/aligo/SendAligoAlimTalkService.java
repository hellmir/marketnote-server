package com.personal.marketnote.notification.service.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.AligoFailoverCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SendAligoAlimTalkExternalPort.AligoAlimTalkSendResponse;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@UseCase
@RequiredArgsConstructor
public class SendAligoAlimTalkService implements SendAligoAlimTalkUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;
    private final SaveAligoMessagePort saveAligoMessagePort;
    private final UpdateAligoMessagePort updateAligoMessagePort;
    private final SendAligoAlimTalkExternalPort sendAligoAlimTalkExternalPort;
    private final ObjectMapper objectMapper;

    @Override
    public SendAligoAlimTalkResult sendAlimTalk(SendAligoAlimTalkCommand command) {
        AligoTemplate template = findApprovedTemplate(command.templateCode());

        String renderedContent = renderContent(template.getTemplateContent(), command.variables());

        String buttonsJson = serializeButtonsFromTemplate(template);

        AligoMessage message = saveMessage(template, command, renderedContent, buttonsJson);

        AligoAlimTalkSendResponse response;
        try {
            response = sendAligoAlimTalkExternalPort.send(message);
        } catch (AligoApiException e) {
            markMessageFailed(message);
            throw e;
        }

        updateMessageAfterSend(message, response);

        return new SendAligoAlimTalkResult(
                message.getId(), response.mid(), response.successCount(), response.failCount()
        );
    }

    private AligoTemplate findApprovedTemplate(String templateCode) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(templateCode)
                .orElseThrow(() -> new AligoTemplateNotFoundException(templateCode));

        AligoTemplateInspectionStatus inspStatus = AligoTemplateInspectionStatus.from(template.getInspStatus());
        if (!inspStatus.isApproved()) {
            throw new AligoTemplateMismatchException(
                    "승인(APR) 상태의 템플릿만 발송할 수 있습니다. inspStatus=" + template.getInspStatus()
            );
        }

        return template;
    }

    private AligoMessage saveMessage(
            AligoTemplate template, SendAligoAlimTalkCommand command,
            String renderedContent, String buttonsJson
    ) {
        List<AligoMessageRecipientCreateState> recipientStates = command.recipients().stream()
                .map(this::toRecipientCreateState)
                .toList();

        AligoMessageCreateState.AligoMessageCreateStateBuilder builder = AligoMessageCreateState.builder()
                .senderKey(template.getSenderKey())
                .templateCode(template.getTemplateCode())
                .sender(command.sender())
                .messageType(AligoMessageType.AT)
                .content(renderedContent)
                .title(template.getTemplateName())
                .buttons(buttonsJson)
                .requestedAt(LocalDateTime.now())
                .recipients(recipientStates);

        if (isFailoverEnabled(command.failover())) {
            builder.failoverSubject(command.failover().subject())
                    .failoverMessageBody(command.failover().body());
        }

        AligoMessage message = AligoMessage.from(builder.build());

        return saveAligoMessagePort.save(message);
    }

    private void updateMessageAfterSend(AligoMessage message, AligoAlimTalkSendResponse response) {
        if (FormatValidator.hasValue(response.smid())) {
            message.markAsFailovered(
                    response.mid(), response.smid(),
                    response.successCount(), response.failCount(), LocalDateTime.now()
            );
        } else {
            message.markAsSent(response.mid(), response.successCount(), response.failCount(), LocalDateTime.now());
        }
        updateAligoMessagePort.update(message);
    }

    private void markMessageFailed(AligoMessage message) {
        message.markAsFailed();
        updateAligoMessagePort.update(message);
    }

    private boolean isFailoverEnabled(AligoFailoverCommand failover) {
        return FormatValidator.hasValue(failover) && failover.enabled();
    }

    private String renderContent(String templateContent, Map<String, String> variables) {
        return AligoTemplateRenderer.render(templateContent, variables);
    }

    private String serializeButtonsFromTemplate(AligoTemplate template) {
        if (FormatValidator.hasNoValue(template.getButtons())) {
            return null;
        }
        List<Map<String, String>> buttonList = template.getButtons().stream()
                .map(this::toButtonMap)
                .toList();
        try {
            return objectMapper.writeValueAsString(Map.of("button", buttonList));
        } catch (JsonProcessingException e) {
            throw new AligoApiRequestFailedException("버튼 직렬화 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, String> toButtonMap(AligoTemplateButton button) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("ordering", String.valueOf(button.getOrdering()));
        map.put("name", button.getName());
        map.put("linkType", button.getLinkType().name());
        if (FormatValidator.hasValue(button.getLinkMo())) {
            map.put("linkMo", button.getLinkMo());
        }
        if (FormatValidator.hasValue(button.getLinkPc())) {
            map.put("linkPc", button.getLinkPc());
        }
        if (FormatValidator.hasValue(button.getLinkIos())) {
            map.put("linkIos", button.getLinkIos());
        }
        if (FormatValidator.hasValue(button.getLinkAnd())) {
            map.put("linkAnd", button.getLinkAnd());
        }
        return map;
    }

    private AligoMessageRecipientCreateState toRecipientCreateState(AligoAlimTalkRecipientCommand command) {
        return AligoMessageRecipientCreateState.builder()
                .receiver(command.receiver())
                .recvName(command.recvName())
                .build();
    }
}
