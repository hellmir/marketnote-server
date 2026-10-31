package com.personal.marketnote.product.utility;

import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;
import com.personal.marketnote.product.port.in.usecase.servicecommunication.RecordProductServiceCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 상품 서비스 간 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class ServiceCommunicationRecorder extends com.personal.marketnote.common.utility.audit.ServiceCommunicationRecorder<
        ProductServiceCommunicationTargetType,
        ProductServiceCommunicationType,
        ProductServiceCommunicationSenderType
> {
    public ServiceCommunicationRecorder(RecordProductServiceCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        ProductServiceCommunicationHistoryCommand.builder()
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
