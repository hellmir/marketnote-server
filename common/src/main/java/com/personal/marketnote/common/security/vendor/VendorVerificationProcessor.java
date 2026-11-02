package com.personal.marketnote.common.security.vendor;

import com.personal.marketnote.common.domain.exception.token.VendorVerificationFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import org.springframework.util.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VendorVerificationProcessor {
    public static void validateSignature(String secret, String text, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacMD5"));
            byte[] result = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));

            if (FormatValidator.notEqualsIgnoreCase(toHex(result), signature)) {
                throw new VendorVerificationFailedException("리워드 지급 연동 중 signed value 검증에 실패했습니다.");
            }
        } catch (Exception e) {
            throw new VendorVerificationFailedException("유효하지 않은 인증키입니다.");
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static void validateSignature(String text, String signature) {
        try {
            String verifyCode = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));

            if (FormatValidator.notEqualsIgnoreCase(verifyCode, signature)) {
                throw new VendorVerificationFailedException("리워드 지급 연동 중 signed value 검증에 실패했습니다.");
            }
        } catch (Exception e) {
            throw new VendorVerificationFailedException("유효하지 않은 인증키입니다.");
        }
    }
}
