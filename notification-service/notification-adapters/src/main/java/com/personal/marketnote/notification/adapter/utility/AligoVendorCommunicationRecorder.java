package com.personal.marketnote.notification.adapter.utility;

import com.personal.marketnote.common.utility.audit.VendorCommunicationHistoryDispatcher;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import com.personal.marketnote.notification.port.in.command.vendorcommunication.NotificationVendorCommunicationHistoryCommand;
import com.personal.marketnote.notification.port.in.usecase.vendorcommunication.NotificationRecordVendorCommunicationHistoryUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aligo 벤더 통신 이력 기록기.
 *
 * <p>공통 {@link com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder}를 상속하여
 * 4종 오버로드를 제공하고, 서비스별 Command 조립은 dispatcher 람다에서 수행한다.</p>
 *
 * <p>FCM 전용 {@code com.personal.marketnote.notification.utility.VendorCommunicationRecorder}와 별도로
 * Aligo 호출(알림톡/친구톡/템플릿/잔액/취소/이력) 영속화를 담당한다.</p>
 */
@Component
public class AligoVendorCommunicationRecorder
        extends com.personal.marketnote.common.utility.audit.VendorCommunicationRecorder<
        NotificationVendorCommunicationTargetType,
        NotificationVendorName,
        NotificationVendorCommunicationType,
        NotificationVendorCommunicationSenderType
        > {
    @Autowired
    public AligoVendorCommunicationRecorder(NotificationRecordVendorCommunicationHistoryUseCase useCase) {
        super((
                targetType, targetId, vendorName, communicationType, sender,
                exception, payload, payloadJson, attemptCount
        ) ->
                useCase.record(
                        NotificationVendorCommunicationHistoryCommand.builder()
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

    public AligoVendorCommunicationRecorder(
            VendorCommunicationHistoryDispatcher<
                    NotificationVendorCommunicationTargetType,
                    NotificationVendorName,
                    NotificationVendorCommunicationType,
                    NotificationVendorCommunicationSenderType
                    > dispatcher
    ) {
        super(dispatcher);
    }
}
