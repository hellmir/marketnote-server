package com.personal.marketnote.fulfillment.utility;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationSenderType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationTargetType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationType;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorName;
import com.personal.marketnote.fulfillment.port.in.command.vendorcommunication.FulfillmentVendorCommunicationHistoryCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendorcommunication.RecordFulfillmentVendorCommunicationHistoryUseCase;
import org.springframework.stereotype.Component;

/**
 * 풀필먼트 벤더 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class VendorCommunicationRecorder extends com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder<
        FulfillmentVendorCommunicationTargetType,
        FulfillmentVendorName,
        FulfillmentVendorCommunicationType,
        FulfillmentVendorCommunicationSenderType
> {
    public VendorCommunicationRecorder(RecordFulfillmentVendorCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, vendorName, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        FulfillmentVendorCommunicationHistoryCommand.builder()
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
