package com.personal.marketnote.commerce.utility;

import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.servicecommunication.RecordCommerceServiceCommunicationHistoryUseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.stereotype.Component;

/**
 * 커머스 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        CommerceServiceCommunicationTargetType,
        CommerceServiceCommunicationType,
        CommerceServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordCommerceServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        CommerceServiceCommunicationHistoryCommand.builder()
                                .targetType(targetType)
                                .targetId(targetId)
                                .communicationType(communicationType)
                                .sender(sender)
                                .exception(exception)
                                .payload(payload)
                                .payloadJson(FormatValidator.hasValue(payloadJson) ? payloadJson.toString() : null)
                                .build()
                )
        );
    }
}
