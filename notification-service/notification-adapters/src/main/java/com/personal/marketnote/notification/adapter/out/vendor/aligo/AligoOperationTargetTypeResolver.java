package com.personal.marketnote.notification.adapter.out.vendor.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.vendorcommunication.NotificationVendorCommunicationTargetType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Aligo 호출의 operation 문자열을 {@link NotificationVendorCommunicationTargetType}으로 매핑한다.
 *
 * <p>매핑 누락 시 fail-open 정책으로 ALIMTALK을 기본값으로 반환하고 ERROR 로그를 남긴다. 시작 단계에서는
 * {@link #validateMappings()}로 알려진 17종 operation의 누락 여부를 검증한다.</p>
 */
@Slf4j
@Component
public class AligoOperationTargetTypeResolver {

    private static final NotificationVendorCommunicationTargetType DEFAULT_TARGET_TYPE =
            NotificationVendorCommunicationTargetType.ALIMTALK;

    private static final Map<String, NotificationVendorCommunicationTargetType> OPERATION_TO_TARGET_TYPE = Map.ofEntries(
            Map.entry("sendAlimTalk", NotificationVendorCommunicationTargetType.ALIMTALK),
            Map.entry("sendBrandTalk", NotificationVendorCommunicationTargetType.FRIEND_TALK),
            Map.entry("createBrandTalkTemplate", NotificationVendorCommunicationTargetType.FRIEND_TALK_TEMPLATE),
            Map.entry("getBrandTalkTemplateList", NotificationVendorCommunicationTargetType.FRIEND_TALK_TEMPLATE),
            Map.entry("requestSenderAuthentication", NotificationVendorCommunicationTargetType.SENDER_PROFILE),
            Map.entry("registerSenderProfile", NotificationVendorCommunicationTargetType.SENDER_PROFILE),
            Map.entry("getSenderProfileList", NotificationVendorCommunicationTargetType.SENDER_PROFILE),
            Map.entry("getHeartInfo", NotificationVendorCommunicationTargetType.BALANCE),
            Map.entry("cancelMessage", NotificationVendorCommunicationTargetType.CANCEL),
            Map.entry("getMessageHistory", NotificationVendorCommunicationTargetType.SEND_HISTORY),
            Map.entry("getMessageDetail", NotificationVendorCommunicationTargetType.SEND_HISTORY),
            Map.entry("getCategoryTree", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE),
            Map.entry("registerTemplate", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE),
            Map.entry("updateTemplate", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE),
            Map.entry("getTemplateList", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE),
            Map.entry("requestTemplateInspection", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE),
            Map.entry("deleteTemplate", NotificationVendorCommunicationTargetType.ALIMTALK_TEMPLATE)
    );

    private static final String[] KNOWN_OPERATIONS = {
            "sendAlimTalk", "sendBrandTalk",
            "createBrandTalkTemplate", "getBrandTalkTemplateList",
            "requestSenderAuthentication", "registerSenderProfile", "getSenderProfileList",
            "getHeartInfo", "cancelMessage",
            "getMessageHistory", "getMessageDetail",
            "getCategoryTree", "registerTemplate", "updateTemplate", "getTemplateList",
            "requestTemplateInspection", "deleteTemplate"
    };

    @PostConstruct
    public void validateMappings() {
        for (String operation : KNOWN_OPERATIONS) {
            if (!OPERATION_TO_TARGET_TYPE.containsKey(operation)) {
                log.error(
                        "Aligo operation→targetType 매핑 누락 감지: operation={} (시작 단계 검증 실패)",
                        operation
                );
            }
        }
    }

    public NotificationVendorCommunicationTargetType resolve(String operation) {
        if (FormatValidator.hasNoValue(operation)) {
            log.error(
                    "Aligo operation이 null/blank로 전달됨: 기본 targetType={} 으로 fail-open 처리",
                    DEFAULT_TARGET_TYPE
            );
            return DEFAULT_TARGET_TYPE;
        }
        NotificationVendorCommunicationTargetType targetType = OPERATION_TO_TARGET_TYPE.get(operation);
        if (FormatValidator.hasNoValue(targetType)) {
            log.error(
                    "Aligo operation→targetType 매핑 미등록: operation={}, 기본 targetType={} 으로 fail-open 처리",
                    operation, DEFAULT_TARGET_TYPE
            );
            return DEFAULT_TARGET_TYPE;
        }
        return targetType;
    }
}
