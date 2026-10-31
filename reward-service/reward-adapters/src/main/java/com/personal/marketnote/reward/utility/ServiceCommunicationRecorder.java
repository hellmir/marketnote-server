package com.personal.marketnote.reward.utility;

import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationSenderType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationTargetType;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationType;
import com.personal.marketnote.reward.port.in.command.servicecommunication.RewardServiceCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.in.usecase.servicecommunication.RecordRewardServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 리워드 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        RewardServiceCommunicationTargetType,
        RewardServiceCommunicationType,
        RewardServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordRewardServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        RewardServiceCommunicationHistoryCommand.builder()
                                .targetType(targetType)
                                .targetId(targetId)
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
