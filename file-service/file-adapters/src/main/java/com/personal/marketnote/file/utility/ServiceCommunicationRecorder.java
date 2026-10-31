package com.personal.marketnote.file.utility;

import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationSenderType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationTargetType;
import com.personal.marketnote.file.domain.servicecommunication.FileServiceCommunicationType;
import com.personal.marketnote.file.port.in.command.servicecommunication.FileServiceCommunicationHistoryCommand;
import com.personal.marketnote.file.port.in.usecase.servicecommunication.RecordFileServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 파일 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        FileServiceCommunicationTargetType,
        FileServiceCommunicationType,
        FileServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordFileServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        FileServiceCommunicationHistoryCommand.builder()
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
