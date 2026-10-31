package com.personal.marketnote.reward.utility;

import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import com.personal.marketnote.reward.port.in.command.vendorcommunication.RewardVendorCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.in.usecase.vendorcommunication.RewardRecordVendorCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 리워드 벤더 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class VendorCommunicationRecorder extends com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder<
        RewardVendorCommunicationTargetType,
        RewardVendorName,
        RewardVendorCommunicationType,
        RewardVendorCommunicationSenderType
> {
    public VendorCommunicationRecorder(RewardRecordVendorCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, vendorName, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        RewardVendorCommunicationHistoryCommand.builder()
                                .targetType(targetType)
                                .targetId(targetId)
                                .vendorName(vendorName)
                                .communicationType(communicationType)
                                .sender(sender)
                                .exception(exception)
                                .payload(payload)
                                .payloadJson(payloadJson)
                                .build()
                )
        );
    }
}
