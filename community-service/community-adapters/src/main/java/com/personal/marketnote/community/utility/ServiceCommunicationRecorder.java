package com.personal.marketnote.community.utility;

import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationSenderType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationTargetType;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationType;
import com.personal.marketnote.community.port.in.command.servicecommunication.CommunityServiceCommunicationHistoryCommand;
import com.personal.marketnote.community.port.in.usecase.servicecommunication.RecordCommunityServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 커뮤니티 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        CommunityServiceCommunicationTargetType,
        CommunityServiceCommunicationType,
        CommunityServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordCommunityServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        CommunityServiceCommunicationHistoryCommand.builder()
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
