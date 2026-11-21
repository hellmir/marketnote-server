package com.personal.marketnote.common.kafka.event;

import com.personal.marketnote.common.utility.FormatValidator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Outbox/Kafka EventEnvelope payload에 포함되는 PII(개인정보) 필드를 직렬화 직전에
 * SHA-256(salt + value)로 단방향 해시하여 평문 유출을 차단하는 유틸리티.
 *
 * <p>로그용 별표 마스킹({@code com.personal.marketnote.common.utility.ValueMasker})과는
 * 용도가 다르다. 별표 마스킹은 사람이 읽는 로그에, 본 해시는 다운스트림 컨슈머가
 * 받는 envelope payload(영구 저장 가능)에 적용된다.
 *
 * <p>Salt는 테넌트(또는 환경)별로 달라야 동일 PII가 envelope 간/환경 간 연결되지 않는다
 * (per-tenant unlinkability).
 */
public final class EnvelopePayloadPiiHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private EnvelopePayloadPiiHasher() {
    }

    public static String hash(String value, String salt) {
        if (FormatValidator.hasNoValue(salt)) {
            throw new IllegalArgumentException("EnvelopePayloadPiiHasher salt must not be null or blank");
        }
        if (FormatValidator.isNull(value)) {
            return null;
        }
        return toHex(digest(salt, value));
    }

    private static byte[] digest(String salt, String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            // salt|value concat 모호성 제거를 위한 0x00 구분자
            digest.update((byte) 0);
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException nsae) {
            // SHA-256은 모든 JVM에 표준 탑재되어 있어 도달 불가능한 경로
            throw new IllegalStateException("SHA-256 algorithm not available", nsae);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            out[i * 2] = HEX[b >>> 4];
            out[i * 2 + 1] = HEX[b & 0x0F];
        }
        return new String(out);
    }
}
