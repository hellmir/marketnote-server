package com.personal.marketnote.reward.adapter.out.offerwall;

import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import com.personal.marketnote.reward.configuration.AdiscopeHashKeyProperties;
import com.personal.marketnote.reward.configuration.AdpopcornHashKeyProperties;
import com.personal.marketnote.reward.configuration.TnkHashKeyProperties;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;
import com.personal.marketnote.reward.exception.RewardTargetInfoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OfferwallSignatureValidationAdapter 테스트")
class OfferwallSignatureValidationAdapterTest {

    private static final String ADPOPCORN_ANDROID_KEY = "adpopcorn-android-secret";
    private static final String ADPOPCORN_IOS_KEY = "adpopcorn-ios-secret";
    private static final String TNK_ANDROID_KEY = "tnk-android-secret";
    private static final String ADISCOPE_ANDROID_KEY = "adiscope-android-secret";

    private AdpopcornHashKeyProperties adpopcornProps;
    private TnkHashKeyProperties tnkProps;
    private AdiscopeHashKeyProperties adiscopeProps;
    private OfferwallSignatureValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        adpopcornProps = new AdpopcornHashKeyProperties();
        adpopcornProps.setAndroid(ADPOPCORN_ANDROID_KEY);
        adpopcornProps.setIos(ADPOPCORN_IOS_KEY);

        tnkProps = new TnkHashKeyProperties();
        tnkProps.setAndroid(TNK_ANDROID_KEY);
        tnkProps.setIos("tnk-ios-secret");

        adiscopeProps = new AdiscopeHashKeyProperties();
        adiscopeProps.setAndroid(ADISCOPE_ANDROID_KEY);
        adiscopeProps.setIos("adiscope-ios-secret");

        adapter = new OfferwallSignatureValidationAdapter(adpopcornProps, tnkProps, adiscopeProps);
    }

    @Nested
    @DisplayName("ADPOPCORN")
    class Adpopcorn {
        private final String userKey = "user-1";
        private final String rewardKey = "reward-1";
        private final Long quantity = 100L;
        private final String campaignKey = "camp-1";

        @Test
        @DisplayName("유효한 HMAC-MD5 서명이면 예외 없이 통과한다")
        void validSignaturePasses() {
            String plainText = userKey + rewardKey + quantity + campaignKey;
            String signature = hmacMd5(ADPOPCORN_ANDROID_KEY, plainText);

            assertThatCode(() -> adapter.validateSignature(
                    OfferwallType.ADPOPCORN, UserDeviceType.ANDROID,
                    signature, userKey, rewardKey, quantity, campaignKey, "KRW"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 서명이면 VendorVerificationFailedException이 발생한다")
        void invalidSignatureFails() {
            assertThatThrownBy(() -> adapter.validateSignature(
                    OfferwallType.ADPOPCORN, UserDeviceType.ANDROID,
                    "wrong-signature", userKey, rewardKey, quantity, campaignKey, "KRW"
            )).isInstanceOf(VendorVerificationFailedException.class);
        }

        @Test
        @DisplayName("디바이스가 ANDROID/IOS로 다르면 서로 다른 키로 검증된다")
        void iosKeyValidates() {
            String plainText = userKey + rewardKey + quantity + campaignKey;
            String iosSignature = hmacMd5(ADPOPCORN_IOS_KEY, plainText);

            assertThatCode(() -> adapter.validateSignature(
                    OfferwallType.ADPOPCORN, UserDeviceType.IOS,
                    iosSignature, userKey, rewardKey, quantity, campaignKey, "KRW"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("TNK")
    class Tnk {
        @Test
        @DisplayName("유효한 MD5 서명이면 예외 없이 통과한다")
        void validSignaturePasses() {
            String userKey = "user-2";
            String rewardKey = "reward-2";
            String plainText = TNK_ANDROID_KEY + userKey + rewardKey;
            String signature = md5Hex(plainText);

            assertThatCode(() -> adapter.validateSignature(
                    OfferwallType.TNK, UserDeviceType.ANDROID,
                    signature, userKey, rewardKey, 50L, "camp", "KRW"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 서명이면 VendorVerificationFailedException이 발생한다")
        void invalidSignatureFails() {
            assertThatThrownBy(() -> adapter.validateSignature(
                    OfferwallType.TNK, UserDeviceType.ANDROID,
                    "wrong", "user", "reward", 50L, "camp", "KRW"
            )).isInstanceOf(VendorVerificationFailedException.class);
        }
    }

    @Nested
    @DisplayName("ADISCOPE")
    class Adiscope {
        @Test
        @DisplayName("유효한 HMAC-MD5 서명이면 예외 없이 통과한다")
        void validSignaturePasses() {
            String userKey = "user-3";
            String rewardUnit = "POINT";
            Long quantity = 200L;
            String rewardKey = "reward-3";
            String plainText = userKey + rewardUnit + quantity + rewardKey;
            String signature = hmacMd5(ADISCOPE_ANDROID_KEY, plainText);

            assertThatCode(() -> adapter.validateSignature(
                    OfferwallType.ADISCOPE, UserDeviceType.ANDROID,
                    signature, userKey, rewardKey, quantity, "camp", rewardUnit
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 서명이면 VendorVerificationFailedException이 발생한다")
        void invalidSignatureFails() {
            assertThatThrownBy(() -> adapter.validateSignature(
                    OfferwallType.ADISCOPE, UserDeviceType.ANDROID,
                    "wrong", "user", "reward", 200L, "camp", "POINT"
            )).isInstanceOf(VendorVerificationFailedException.class);
        }
    }

    @Nested
    @DisplayName("해시 키 해결")
    class HashKeyResolution {
        @Test
        @DisplayName("설정된 디바이스 해시 키가 비어 있으면 RewardTargetInfoNotFoundException이 발생한다")
        void throwsWhenDeviceHashKeyMissing() {
            AdpopcornHashKeyProperties props = new AdpopcornHashKeyProperties();
            props.setAndroid(ADPOPCORN_ANDROID_KEY);
            props.setIos(null);
            OfferwallSignatureValidationAdapter customAdapter = new OfferwallSignatureValidationAdapter(
                    props, tnkProps, adiscopeProps
            );

            assertThatThrownBy(() -> customAdapter.validateSignature(
                    OfferwallType.ADPOPCORN, UserDeviceType.IOS,
                    "sig", "user", "reward", 10L, "camp", "KRW"
            )).isInstanceOf(RewardTargetInfoNotFoundException.class);
        }
    }

    private static String hmacMd5(String secret, String text) {
        try {
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacMD5"));
            byte[] result = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String md5Hex(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
}
