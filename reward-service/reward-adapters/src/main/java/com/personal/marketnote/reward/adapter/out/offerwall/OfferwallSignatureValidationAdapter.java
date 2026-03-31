package com.personal.marketnote.reward.adapter.out.offerwall;

import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import com.personal.marketnote.common.security.vendor.VendorVerificationProcessor;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.configuration.AdiscopeHashKeyProperties;
import com.personal.marketnote.reward.configuration.AdpopcornHashKeyProperties;
import com.personal.marketnote.reward.configuration.TnkHashKeyProperties;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.exception.InvalidOfferwallTypeException;
import com.personal.marketnote.reward.exception.RewardTargetInfoNotFoundException;
import com.personal.marketnote.reward.port.out.offerwall.ValidateOfferwallSignaturePort;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class OfferwallSignatureValidationAdapter implements ValidateOfferwallSignaturePort {
    private final AdpopcornHashKeyProperties adpopcornHashKeyProperties;
    private final TnkHashKeyProperties tnkHashKeyProperties;
    private final AdiscopeHashKeyProperties adiscopeHashKeyProperties;

    @Override
    public void validateSignature(
            OfferwallType offerwallType,
            UserDeviceType userDeviceType,
            String signedValue,
            String userKey,
            String rewardKey,
            Long quantity,
            String campaignKey,
            String rewardUnit
    ) {
        String hashKey = resolveHashKey(offerwallType, userDeviceType);
        String plainText = buildPlainText(offerwallType, hashKey, userKey, rewardKey, quantity, campaignKey, rewardUnit);

        Map<OfferwallType, Runnable> signatureValidators = buildSignatureValidators(
                hashKey, plainText, signedValue
        );

        Runnable validator = signatureValidators.get(offerwallType);
        if (FormatValidator.hasNoValue(validator)) {
            throw new InvalidOfferwallTypeException(offerwallType.name());
        }

        validator.run();
    }

    private Map<OfferwallType, Runnable> buildSignatureValidators(
            String hashKey,
            String plainText,
            String signedValue
    ) {
        Map<OfferwallType, Runnable> validators = new EnumMap<>(OfferwallType.class);
        validators.put(OfferwallType.ADPOPCORN, () -> VendorVerificationProcessor.validateSignature(hashKey, plainText, signedValue));
        validators.put(OfferwallType.TNK, () -> VendorVerificationProcessor.validateSignature(plainText, signedValue));
        validators.put(OfferwallType.ADISCOPE, () -> VendorVerificationProcessor.validateSignature(hashKey, plainText, signedValue));
        return validators;
    }

    private String buildPlainText(
            OfferwallType offerwallType,
            String hashKey,
            String userKey,
            String rewardKey,
            Long quantity,
            String campaignKey,
            String rewardUnit
    ) {
        Map<OfferwallType, Function<Void, String>> plainTextBuilders = new EnumMap<>(OfferwallType.class);
        plainTextBuilders.put(OfferwallType.ADPOPCORN, unused -> userKey + rewardKey + quantity + campaignKey);
        plainTextBuilders.put(OfferwallType.TNK, unused -> hashKey + userKey + rewardKey);
        plainTextBuilders.put(OfferwallType.ADISCOPE, unused -> userKey + rewardUnit + quantity + rewardKey);

        Function<Void, String> builder = plainTextBuilders.get(offerwallType);
        if (FormatValidator.hasNoValue(builder)) {
            throw new InvalidOfferwallTypeException(offerwallType.name());
        }

        return builder.apply(null);
    }

    private String resolveHashKey(OfferwallType offerwallType, UserDeviceType userDeviceType) {
        Map<OfferwallType, Map<UserDeviceType, String>> hashKeyMap = buildHashKeyMap();

        Map<UserDeviceType, String> deviceHashKeys = hashKeyMap.get(offerwallType);
        if (FormatValidator.hasNoValue(deviceHashKeys)) {
            throw new InvalidOfferwallTypeException(offerwallType.name());
        }

        String hashKey = deviceHashKeys.get(userDeviceType);
        if (FormatValidator.hasNoValue(hashKey)) {
            throw new RewardTargetInfoNotFoundException("오퍼월 리워드 지급 대상 디바이스 정보가 없습니다.");
        }

        return requireHashKey(hashKey);
    }

    private Map<OfferwallType, Map<UserDeviceType, String>> buildHashKeyMap() {
        Map<OfferwallType, Map<UserDeviceType, String>> hashKeyMap = new EnumMap<>(OfferwallType.class);

        Map<UserDeviceType, String> adpopcornKeys = new EnumMap<>(UserDeviceType.class);
        adpopcornKeys.put(UserDeviceType.ANDROID, adpopcornHashKeyProperties.getAndroid());
        adpopcornKeys.put(UserDeviceType.IOS, adpopcornHashKeyProperties.getIos());
        hashKeyMap.put(OfferwallType.ADPOPCORN, adpopcornKeys);

        Map<UserDeviceType, String> tnkKeys = new EnumMap<>(UserDeviceType.class);
        tnkKeys.put(UserDeviceType.ANDROID, tnkHashKeyProperties.getAndroid());
        tnkKeys.put(UserDeviceType.IOS, tnkHashKeyProperties.getIos());
        hashKeyMap.put(OfferwallType.TNK, tnkKeys);

        Map<UserDeviceType, String> adiscopeKeys = new EnumMap<>(UserDeviceType.class);
        adiscopeKeys.put(UserDeviceType.ANDROID, adiscopeHashKeyProperties.getAndroid());
        adiscopeKeys.put(UserDeviceType.IOS, adiscopeHashKeyProperties.getIos());
        hashKeyMap.put(OfferwallType.ADISCOPE, adiscopeKeys);

        return hashKeyMap;
    }

    private String requireHashKey(String hashKey) {
        if (FormatValidator.hasValue(hashKey)) {
            return hashKey;
        }

        throw new VendorVerificationFailedException("오퍼월 해시 키가 설정되지 않았습니다.");
    }
}
