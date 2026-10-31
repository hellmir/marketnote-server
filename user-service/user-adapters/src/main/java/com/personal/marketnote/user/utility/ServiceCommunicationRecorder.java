package com.personal.marketnote.user.utility;

import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationSenderType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationTargetType;
import com.personal.marketnote.user.domain.servicecommunication.UserServiceCommunicationType;
import com.personal.marketnote.user.port.in.command.servicecommunication.UserServiceCommunicationHistoryCommand;
import com.personal.marketnote.user.port.in.usecase.servicecommunication.RecordUserServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 유저 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        UserServiceCommunicationTargetType,
        UserServiceCommunicationType,
        UserServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordUserServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        UserServiceCommunicationHistoryCommand.builder()
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
