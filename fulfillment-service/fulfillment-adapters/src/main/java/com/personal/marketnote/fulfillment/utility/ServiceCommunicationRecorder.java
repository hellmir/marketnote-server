package com.personal.marketnote.fulfillment.utility;

import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationType;
import com.personal.marketnote.fulfillment.port.in.command.servicecommunication.FulfillmentServiceCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.servicecommunication.RecordFulfillmentServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 풀필먼트 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        FulfillmentServiceCommunicationTargetType,
        FulfillmentServiceCommunicationType,
        FulfillmentServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordFulfillmentServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        FulfillmentServiceCommunicationHistoryCommand.builder()
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
