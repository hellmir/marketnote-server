package com.personal.marketnote.common.security.hmac;

import com.personal.marketnote.common.security.hmac.exception.HmacSignatureGenerationFailedException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static com.personal.marketnote.common.security.hmac.HmacHeaderConstants.SIGNING_INPUT_DELIMITER;

public final class HmacSignatureGenerator {
    private static final String HMAC_MD5_ALGORITHM = "HmacMD5";

    private HmacSignatureGenerator() {
    }

    public static String generate(String secretKey,
                                  String timestamp,
                                  String nonce,
                                  String httpMethod,
                                  String requestPath) {
        String signingInput = buildSigningInput(timestamp, nonce, httpMethod, requestPath);
        return computeHmacMd5(secretKey, signingInput);
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    static String buildSigningInput(String timestamp,
                                    String nonce,
                                    String httpMethod,
                                    String requestPath) {
        return timestamp
                + SIGNING_INPUT_DELIMITER + nonce
                + SIGNING_INPUT_DELIMITER + httpMethod
                + SIGNING_INPUT_DELIMITER + requestPath;
    }

    private static String computeHmacMd5(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_MD5_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_MD5_ALGORITHM));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (Exception e) {
            throw new HmacSignatureGenerationFailedException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
