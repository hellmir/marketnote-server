package com.personal.marketnote.reward.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 오퍼월(Adpopcorn/Tnk/Adiscope) 벤더 통신 페이로드 생성기.
 *
 * <p>광고 네트워크별 고유 필드 포맷을 가진 페이로드를 조립한다. 공통 벤더 통신 페이로드(error/message)는
 * {@link com.personal.marketnote.common.utility.audit.VendorCommunicationPayloadGenerator}를 사용한다.</p>
 */
@Component
@RequiredArgsConstructor
public class OfferwallVendorCommunicationPayloadGenerator {
    private final ObjectMapper objectMapper;

    public JsonNode buildResponsePayloadJson(boolean isSuccess, int resultCode, String resultMessage) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("Result", isSuccess);
        node.put("ResultCode", resultCode);
        node.put("ResultMsg", resultMessage);

        return node;
    }

    public JsonNode buildAdpopcornPayloadJson(
            String rewardKey,
            String userKey,
            UserDeviceType userDeviceType,
            String campaignKey,
            Integer campaignType,
            String campaignName,
            Long quantity,
            String signedValue,
            Integer appKey,
            String appName,
            String adid,
            String idfa,
            String attendedAt
    ) {
        ObjectNode node = objectMapper.createObjectNode();

        putIfNotBlank(node, "reward_key", rewardKey);
        putIfNotBlank(node, "usn", userKey);
        putIfNotBlank(node, "user_device_type", userDeviceType.name());
        putIfNotBlank(node, "campaign_key", campaignKey);
        putIfNotBlank(node, "campaign_type", campaignType);
        putIfNotBlank(node, "campaign_name", campaignName);
        putIfNotBlank(node, "quantity", quantity);
        putIfNotBlank(node, "signed_value", signedValue);
        putIfNotBlank(node, "app_key", appKey);
        putIfNotBlank(node, "app_name", appName);
        putIfNotBlank(node, "adid", adid);
        putIfNotBlank(node, "idfa", idfa);
        putIfNotBlank(node, "time_stamp", attendedAt);

        return node;
    }

    public JsonNode buildTnkPayloadJson(
            String rewardKey,
            String userKey,
            UserDeviceType userDeviceType,
            Integer campaignType,
            Long quantity,
            String signedValue,
            String appName,
            String adid,
            String attendedAt,
            BigDecimal revenue
    ) {
        ObjectNode node = objectMapper.createObjectNode();

        putIfNotBlank(node, "seq_id", rewardKey);
        putIfNotBlank(node, "md_user_nm", userKey);
        putIfNotBlank(node, "user_device_type", userDeviceType.name());
        putIfNotBlank(node, "campaign_type", campaignType);
        putIfNotBlank(node, "pay_pnt", quantity);
        putIfNotBlank(node, "md_chk", signedValue);
        putIfNotBlank(node, "app_nm", appName);
        putIfNotBlank(node, "app_id", adid);
        putIfNotBlank(node, "pay_dt", attendedAt);
        putIfNotBlank(node, "pay_amt", revenue);

        return node;
    }

    public JsonNode buildAdiscopePayloadJson(
            String rewardKey,
            String userKey,
            UserDeviceType userDeviceType,
            String campaignKey,
            String campaignType,
            String campaignName,
            String rewardUnit,
            Long quantity,
            String signedValue,
            String adid,
            String network
    ) {
        ObjectNode node = objectMapper.createObjectNode();

        putIfNotBlank(node, "transactionId", rewardKey);
        putIfNotBlank(node, "userId", userKey);
        putIfNotBlank(node, "user_device_type", userDeviceType.name());
        putIfNotBlank(node, "unitId", campaignKey);
        putIfNotBlank(node, "shareAdType", campaignType);
        putIfNotBlank(node, "adname", campaignName);
        putIfNotBlank(node, "rewardUnit", rewardUnit);
        putIfNotBlank(node, "rewardAmount", quantity);
        putIfNotBlank(node, "signature", signedValue);
        putIfNotBlank(node, "adid", adid);
        putIfNotBlank(node, "network", network);

        return node;
    }

    private void putIfNotBlank(ObjectNode node, String field, String value) {
        if (StringUtils.hasText(value)) {
            node.put(field, value);
        }
    }

    private void putIfNotBlank(ObjectNode node, String field, Number value) {
        if (FormatValidator.hasValue(value)) {
            node.put(field, value.toString());
        }
    }
}
