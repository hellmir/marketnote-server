package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.external.log.ExternalCallLogger;
import com.personal.marketnote.common.utility.audit.VendorCommunicationHistoryDispatcher;
import com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationFailureHandler;
import com.personal.marketnote.notification.adapter.utility.AligoVendorCommunicationRecorder;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationSenderType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationType;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorName;
import org.springframework.web.client.RestClient;

/**
 * AligoHttpClient 테스트용 보조 인스턴스 생성기.
 *
 * <p>기존 테스트들이 4-인자 생성자에 의존하던 것을 신규 의존성(Recorder/FailureHandler/PayloadGenerator/Resolver)을
 * no-op 구현으로 채워 넣어 컴파일을 유지한다.</p>
 */
public final class AligoHttpClientTestSupport {

    private AligoHttpClientTestSupport() {
    }

    public static AligoHttpClient newClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            AligoProperties properties,
            ExternalCallLogger externalCallLogger
    ) {
        return newClient(
                restClient, objectMapper, properties, externalCallLogger,
                (targetType, targetId, vendorName, communicationType, sender, exception, payload, payloadJson) -> {
                    // no-op
                }
        );
    }

    public static AligoHttpClient newClient(
            RestClient restClient,
            ObjectMapper objectMapper,
            AligoProperties properties,
            ExternalCallLogger externalCallLogger,
            VendorCommunicationHistoryDispatcher<
                    NotificationVendorCommunicationTargetType,
                    NotificationVendorName,
                    NotificationVendorCommunicationType,
                    NotificationVendorCommunicationSenderType
                    > dispatcher
    ) {
        AligoVendorCommunicationRecorder recorder = new AligoVendorCommunicationRecorder(dispatcher);
        VendorCommunicationPayloadGenerator payloadGenerator =
                new VendorCommunicationPayloadGenerator(objectMapper);
        AligoVendorCommunicationFailureHandler failureHandler =
                new AligoVendorCommunicationFailureHandler(recorder, payloadGenerator);
        AligoOperationTargetTypeResolver resolver = new AligoOperationTargetTypeResolver();
        return new AligoHttpClient(
                restClient, objectMapper, properties, externalCallLogger,
                recorder, failureHandler, payloadGenerator, resolver
        );
    }
}
