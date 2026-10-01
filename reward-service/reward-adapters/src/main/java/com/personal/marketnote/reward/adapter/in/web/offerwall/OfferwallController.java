package com.personal.marketnote.reward.adapter.in.web.offerwall;

import com.fasterxml.jackson.databind.JsonNode;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.in.web.offerwall.apidocs.AdiscopeCallbackApiDocs;
import com.personal.marketnote.reward.adapter.in.web.offerwall.apidocs.AdpopcornCallbackApiDocs;
import com.personal.marketnote.reward.adapter.in.web.offerwall.apidocs.TnkCallbackApiDocs;
import com.personal.marketnote.reward.adapter.in.web.point.response.UpdateUserPointResponse;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationSenderType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationType;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorName;
import com.personal.marketnote.reward.exception.DuplicateOfferwallRewardException;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.usecase.offerwall.HandleOfferwallRewardUseCase;
import com.personal.marketnote.reward.utility.VendorCommunicationFailureHandler;
import com.personal.marketnote.reward.utility.VendorCommunicationPayloadGenerator;
import com.personal.marketnote.reward.utility.VendorCommunicationRecorder;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType.OFFERWALL;

@RestController
@RequestMapping("/api/v1/offerwalls")
@Tag(name = "오퍼월 API", description = "오퍼월 관련 API")
@RequiredArgsConstructor
@Slf4j
public class OfferwallController {
    private final HandleOfferwallRewardUseCase handleOfferwallRewardUseCase;
    private final VendorCommunicationRecorder vendorCommunicationRecorder;
    private final VendorCommunicationFailureHandler vendorCommunicationFailureHandler;
    private final VendorCommunicationPayloadGenerator vendorCommunicationPayloadGenerator;

    /**
     * 애드팝콘 리워드 지급 콜백 엔드포인트
     *
     * @param rewardKey      리워드 키
     * @param userKey        회원 키
     * @param userDeviceType 회원 기기 유형
     * @param campaignKey    캠페인 키
     * @param campaignType   캠페인 유형
     * @param campaignName   캠페인 이름
     * @param quantity       수량
     * @param signedValue    서명 값(검증 필요)
     * @param appKey         앱 키
     * @param appName        앱 이름
     * @param adid           구글 광고 ID
     * @param idfa           애플 광고 ID
     * @param attendedAt     캠페인 완료일시
     * @Author 성효빈
     * @Date 2026-01-16
     */
    @PostMapping(value = "/adpopcorn/callback")
    @AdpopcornCallbackApiDocs
    public ResponseEntity<String> handleAdpopcornCallback(
            @RequestParam("reward_key") String rewardKey,
            @RequestParam("usn") String userKey,
            @RequestParam("user_device_type") UserDeviceType userDeviceType,
            @RequestParam("campaign_key") String campaignKey,
            @RequestParam(value = "campaign_type", required = false) Integer campaignType,
            @RequestParam(value = "campaign_name", required = false) String campaignName,
            @RequestParam(value = "quantity") Long quantity,
            @RequestParam(value = "signed_value") String signedValue,
            @RequestParam(value = "app_key", required = false) Integer appKey,
            @RequestParam(value = "app_name", required = false) String appName,
            @RequestParam(value = "adid", required = false) String adid,
            @RequestParam(value = "idfa", required = false) String idfa,
            @RequestParam(value = "time_stamp", required = false) String attendedAt
    ) {
        LocalDateTime parsedAttendedAt = FormatConverter.parseToLocalDateTime(attendedAt);

        JsonNode payloadJson = vendorCommunicationPayloadGenerator.buildAdpopcornPayloadJson(
                rewardKey,
                userKey,
                userDeviceType,
                campaignKey,
                campaignType,
                campaignName,
                quantity,
                signedValue,
                appKey,
                appName,
                adid,
                idfa,
                attendedAt
        );
        String payloadString = payloadJson.toString();

        RegisterOfferwallRewardCommand command = RegisterOfferwallRewardCommand.builder()
                .offerwallType(OfferwallType.ADPOPCORN)
                .rewardKey(rewardKey)
                .userKey(userKey)
                .userDeviceType(userDeviceType)
                .campaignKey(campaignKey)
                .campaignType(campaignType)
                .campaignName(campaignName)
                .quantity(quantity)
                .signedValue(signedValue)
                .appKey(appKey)
                .appName(appName)
                .adid(adid)
                .idfa(idfa)
                .attendedAt(parsedAttendedAt)
                .build();

        RewardVendorCommunicationTargetType targetType = OFFERWALL;
        RewardVendorName vendorName = RewardVendorName.ADPOPCORN;

        try {
            Long id = handleOfferwallRewardUseCase.handle(command);
            JsonNode successPayloadJson = vendorCommunicationPayloadGenerator.buildResponsePayloadJson(
                    true, 1, "success"
            );
            String successPayload = successPayloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.REQUEST,
                    RewardVendorCommunicationSenderType.VENDOR,
                    String.valueOf(id),
                    vendorName,
                    payloadString,
                    payloadJson
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.RESPONSE,
                    RewardVendorCommunicationSenderType.SERVER,
                    String.valueOf(id),
                    vendorName,
                    successPayload,
                    successPayloadJson
            );

            return ResponseEntity.ok(successPayload);
        } catch (VendorVerificationFailedException e) {
            // 서명 검증 실패
            return vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 1100, "invalid signed value"
            );
        } catch (UserNotFoundException e) {
            // 회원 포인트 도메인 정보 조회 실패
            return vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3200, "invalid user"
            );
        } catch (DuplicateOfferwallRewardException e) {
            // 중복 리워드 지급 시도
            return vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3100, "duplicate transaction"
            );
        } catch (Exception e) {
            // 그 외
            return vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 4000, "custom error message"
            );
        }
    }

    /**
     * TNK 리워드 지급 콜백 엔드포인트
     *
     * @param rewardKey      리워드 키
     * @param userKey        회원 키
     * @param userDeviceType 회원 기기 유형
     * @param campaignType   캠페인 유형
     * @param quantity       수량
     * @param signedValue    서명 값(검증 필요)
     * @param appName        앱 이름
     * @param adid           광고 ID
     * @param attendedAt     캠페인 완료일시
     * @param revenue        리워드 수익
     * @Author 성효빈
     * @Date 2026-01-19
     */
    @PostMapping(value = "/tnk/callback")
    @TnkCallbackApiDocs
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> handleTnkCallback(
            @RequestParam("seq_id") String rewardKey,
            @RequestParam("md_user_nm") String userKey,
            @RequestParam("user_device_type") UserDeviceType userDeviceType,
            @RequestParam(value = "actn_id", required = false) Integer campaignType,
            @RequestParam(value = "pay_pnt") Long quantity,
            @RequestParam(value = "md_chk") String signedValue,
            @RequestParam(value = "app_nm", required = false) String appName,
            @RequestParam(value = "app_id", required = false) String adid,
            @RequestParam(value = "pay_dt", required = false) String attendedAt,
            @RequestParam(value = "pay_amt", required = true) BigDecimal revenue
    ) {
        LocalDateTime parsedAttendedAt = FormatConverter.parseToLocalDateTime(attendedAt);

        JsonNode payloadJson = vendorCommunicationPayloadGenerator.buildTnkPayloadJson(
                rewardKey,
                userKey,
                userDeviceType,
                campaignType,
                quantity,
                signedValue,
                appName,
                adid,
                attendedAt,
                revenue
        );
        String payloadString = payloadJson.toString();

        RegisterOfferwallRewardCommand command = RegisterOfferwallRewardCommand.builder()
                .offerwallType(OfferwallType.TNK)
                .rewardKey(rewardKey)
                .userKey(userKey)
                .userDeviceType(userDeviceType)
                .campaignType(campaignType)
                .quantity(quantity)
                .signedValue(signedValue)
                .appName(appName)
                .adid(adid)
                .idfa(adid)
                .attendedAt(parsedAttendedAt)
                .build();

        RewardVendorCommunicationTargetType targetType = OFFERWALL;
        RewardVendorName vendorName = RewardVendorName.TNK;

        try {
            Long id = handleOfferwallRewardUseCase.handle(command);
            JsonNode successPayloadJson = vendorCommunicationPayloadGenerator.buildResponsePayloadJson(
                    true, 1, "success"
            );
            String successPayload = successPayloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.REQUEST,
                    RewardVendorCommunicationSenderType.VENDOR,
                    String.valueOf(id),
                    vendorName,
                    payloadString,
                    payloadJson
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.RESPONSE,
                    RewardVendorCommunicationSenderType.SERVER,
                    String.valueOf(id),
                    vendorName,
                    successPayload,
                    successPayloadJson
            );

            return ResponseEntity.ok(
                    BaseResponse.of(
                            HttpStatus.OK,
                            DEFAULT_SUCCESS_CODE,
                            "TNK 리워드 포인트 지급 성공"
                    )
            );
        } catch (VendorVerificationFailedException e) {
            // 서명 검증 실패
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 1100, "invalid signed value"
            );
            throw e;
        } catch (UserNotFoundException e) {
            // 회원 포인트 도메인 정보 조회 실패
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3200, "invalid user"
            );
            throw e;
        } catch (DuplicateOfferwallRewardException e) {
            // 중복 리워드 지급 시도
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3100, "duplicate transaction"
            );
            throw e;
        } catch (Exception e) {
            // 그 외
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 4000, "custom error message"
            );
            throw e;
        }
    }

    /**
     * 애디스콥 리워드 지급 콜백 엔드포인트
     *
     * @param rewardKey      리워드 키
     * @param userKey        회원 키
     * @param userDeviceType 회원 기기 유형
     * @param campaignKey    캠페인 키
     * @param campaignType   캠페인 유형
     * @param campaignName   캠페인 이름
     * @param rewardUnit     보상 화폐 단위
     * @param quantity       수량
     * @param signedValue    서명 값(검증 필요)
     * @param adid           광고 ID
     * @param network        보상 지급 네트워크사
     * @Author 성효빈
     * @Date 2026-01-20
     */
    @GetMapping(value = "/adiscope/callback")
    @AdiscopeCallbackApiDocs
    public ResponseEntity<BaseResponse<UpdateUserPointResponse>> handleAdiscopeCallback(
            @RequestParam("transactionId") String rewardKey,
            @RequestParam("userId") String userKey,
            @RequestParam("user_device_type") UserDeviceType userDeviceType,
            @RequestParam(value = "unitId", required = false) String campaignKey,
            @RequestParam(value = "shareAdType", required = false) String campaignType,
            @RequestParam(value = "adname", required = false) String campaignName,
            @RequestParam(value = "rewardUnit", required = false) String rewardUnit,
            @RequestParam(value = "rewardAmount") Long quantity,
            @RequestParam(value = "signature") String signedValue,
            @RequestParam(value = "adid", required = false) String adid,
            @RequestParam(value = "network", required = false) String network
    ) {
        LocalDateTime attendedAt = LocalDateTime.now();

        JsonNode payloadJson = vendorCommunicationPayloadGenerator.buildAdiscopePayloadJson(
                rewardKey,
                userKey,
                userDeviceType,
                campaignKey,
                campaignType,
                campaignName,
                rewardUnit,
                quantity,
                signedValue,
                adid,
                network
        );
        String payloadString = payloadJson.toString();

        RegisterOfferwallRewardCommand command = RegisterOfferwallRewardCommand.builder()
                .offerwallType(OfferwallType.ADISCOPE)
                .rewardUnit(rewardUnit)
                .rewardKey(rewardKey)
                .userKey(userKey)
                .userDeviceType(userDeviceType)
                .campaignKey(campaignKey)
                .campaignType(
                        FormatValidator.equals(campaignType, OFFERWALL.name())
                                ? 0
                                : 1
                )
                .campaignName(campaignName)
                .quantity(quantity)
                .signedValue(signedValue)
                .adid(adid)
                .idfa(adid)
                .attendedAt(attendedAt)
                .build();

        RewardVendorCommunicationTargetType targetType = OFFERWALL;
        RewardVendorName vendorName = RewardVendorName.ADISCOPE;

        try {
            Long id = handleOfferwallRewardUseCase.handle(command);
            JsonNode successPayloadJson = vendorCommunicationPayloadGenerator.buildResponsePayloadJson(
                    true, 1, "success"
            );
            String successPayload = successPayloadJson.toString();

            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.REQUEST,
                    RewardVendorCommunicationSenderType.VENDOR,
                    String.valueOf(id),
                    vendorName,
                    payloadString,
                    payloadJson
            );
            vendorCommunicationRecorder.record(
                    targetType,
                    RewardVendorCommunicationType.RESPONSE,
                    RewardVendorCommunicationSenderType.SERVER,
                    String.valueOf(id),
                    vendorName,
                    successPayload,
                    successPayloadJson
            );

            return ResponseEntity.ok(
                    BaseResponse.of(
                            HttpStatus.OK,
                            DEFAULT_SUCCESS_CODE,
                            "애디스콥 리워드 포인트 지급 성공"
                    )
            );
        } catch (VendorVerificationFailedException e) {
            // 서명 검증 실패
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 1100, "invalid signed value"
            );
            throw e;
        } catch (UserNotFoundException e) {
            // 회원 포인트 도메인 정보 조회 실패
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3200, "invalid user"
            );
            throw e;
        } catch (DuplicateOfferwallRewardException e) {
            // 중복 리워드 지급 시도
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 3100, "duplicate transaction"
            );
            throw e;
        } catch (Exception e) {
            // 그 외
            vendorCommunicationFailureHandler.handleFailure(
                    targetType, vendorName, payloadString, payloadJson, e, 4000, "custom error message"
            );
            throw e;
        }
    }
}
