package com.personal.marketnote.commerce.utility;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationSenderType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationTargetType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationType;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorName;
import com.personal.marketnote.commerce.port.in.command.vendorcommunication.CommerceVendorCommunicationHistoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.vendorcommunication.RecordCommerceVendorCommunicationHistoryUseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.stereotype.Component;

/**
 * 커머스 벤더 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 */
@Component
public class VendorCommunicationRecorder extends com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder<
        CommerceVendorCommunicationTargetType,
        CommerceVendorName,
        CommerceVendorCommunicationType,
        CommerceVendorCommunicationSenderType
> {
    public VendorCommunicationRecorder(RecordCommerceVendorCommunicationHistoryUseCase useCase) {
        super((targetType, targetId, vendorName, communicationType, sender, exception, payload, payloadJson) ->
                useCase.record(
                        CommerceVendorCommunicationHistoryCommand.builder()
                                .targetType(targetType)
                                .targetId(targetId)
                                .vendorName(vendorName)
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
