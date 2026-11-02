package com.personal.marketnote.notification.service.aligo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.BrandTalkFailoverCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendBrandTalkUseCase;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkMessagePort;
import com.personal.marketnote.notification.port.out.aligo.SendBrandTalkExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateBrandTalkMessagePort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@UseCase
@RequiredArgsConstructor
public class SendBrandTalkService implements SendBrandTalkUseCase {

    private final SaveBrandTalkMessagePort saveBrandTalkMessagePort;
    private final UpdateBrandTalkMessagePort updateBrandTalkMessagePort;
    private final SendBrandTalkExternalPort sendBrandTalkExternalPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public SendBrandTalkResult sendBrandTalk(SendBrandTalkCommand command) {
        AligoBrandTalkMessage message = createMessage(command);

        AligoBrandTalkMessage saved = saveBrandTalkMessagePort.save(message);

        List<byte[]> failoverImages = resolveFailoverImages(command.failover());
        List<String> failoverImageFileNames = resolveFailoverImageFileNames(command.failover());

        SendBrandTalkExternalPort.BrandTalkSendResponse response = sendToExternal(saved, failoverImages, failoverImageFileNames);

        markAsSent(saved, response);
        updateBrandTalkMessagePort.update(saved);

        return toResult(saved);
    }

    private AligoBrandTalkMessage createMessage(SendBrandTalkCommand command) {
        BrandTalkKakaoTarget kakaoTarget = BrandTalkKakaoTarget.from(command.kakaoTarget());
        BrandTalkFailoverType failoverType = resolveFailoverType(command.failover());

        List<AligoBrandTalkMessageRecipientCreateState> recipientStates = command.recipients().stream()
                .map(this::toRecipientCreateState)
                .toList();

        return AligoBrandTalkMessage.from(
                AligoBrandTalkMessageCreateState.builder()
                        .senderKey(command.senderKey())
                        .sender(command.sender())
                        .kakaoTarget(kakaoTarget)
                        .advertYn(command.advertYn())
                        .templateCode(command.templateCode())
                        .failoverEnabled(isFailoverEnabled(command.failover()))
                        .failoverSubject(resolveFailoverSubject(command.failover()))
                        .failoverType(failoverType)
                        .requestedAt(LocalDateTime.now(clock))
                        .recipients(recipientStates)
                        .build()
        );
    }

    private AligoBrandTalkMessageRecipientCreateState toRecipientCreateState(BrandTalkRecipientCommand recipient) {
        String receiverMessage = serializeVariables(recipient.variables());
        return AligoBrandTalkMessageRecipientCreateState.builder()
                .receiver(recipient.receiver())
                .receiverMessage(receiverMessage)
                .build();
    }

    private String serializeVariables(Map<String, String> variables) {
        if (FormatValidator.hasNoValue(variables) || variables.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            throw new InvalidBrandTalkMessageException("수신자 변수 직렬화 실패: " + e.getMessage());
        }
    }

    private SendBrandTalkExternalPort.BrandTalkSendResponse sendToExternal(
            AligoBrandTalkMessage saved, List<byte[]> failoverImages, List<String> failoverImageFileNames
    ) {
        try {
            return sendBrandTalkExternalPort.send(saved, failoverImages, failoverImageFileNames);
        } catch (Exception e) {
            saved.markAsFailed();
            updateBrandTalkMessagePort.update(saved);
            throw e;
        }
    }

    private void markAsSent(AligoBrandTalkMessage message, SendBrandTalkExternalPort.BrandTalkSendResponse response) {
        BrandTalkMessageCostType costType = BrandTalkMessageCostType.from(response.costType());
        message.markAsSent(
                response.mid(), costType,
                response.unitCost(), response.totalCost(),
                response.successCount(), response.failCount(),
                LocalDateTime.now(clock)
        );
    }

    private SendBrandTalkResult toResult(AligoBrandTalkMessage message) {
        return new SendBrandTalkResult(
                message.getId(),
                message.getAligoMid(),
                message.getSuccessCount(),
                message.getFailCount(),
                message.getCostType().name(),
                message.getUnitCost(),
                message.getTotalCost()
        );
    }

    private boolean isFailoverEnabled(BrandTalkFailoverCommand failover) {
        if (FormatValidator.hasNoValue(failover)) {
            return false;
        }
        return failover.enabled();
    }

    private String resolveFailoverSubject(BrandTalkFailoverCommand failover) {
        if (FormatValidator.hasNoValue(failover)) {
            return null;
        }
        return failover.subject();
    }

    private BrandTalkFailoverType resolveFailoverType(BrandTalkFailoverCommand failover) {
        if (FormatValidator.hasNoValue(failover) || !failover.enabled()) {
            return null;
        }
        return BrandTalkFailoverType.from(failover.failoverType());
    }

    private List<byte[]> resolveFailoverImages(BrandTalkFailoverCommand failover) {
        if (FormatValidator.hasNoValue(failover) || FormatValidator.hasNoValue(failover.mmsImages())) {
            return List.of();
        }
        return failover.mmsImages();
    }

    private List<String> resolveFailoverImageFileNames(BrandTalkFailoverCommand failover) {
        if (FormatValidator.hasNoValue(failover) || FormatValidator.hasNoValue(failover.mmsImageFileNames())) {
            return List.of();
        }
        return failover.mmsImageFileNames();
    }
}
