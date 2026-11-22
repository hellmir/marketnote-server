package com.personal.marketnote.product.domain.product;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Product 도메인 식별자(productKey UUID)를 Fassto 운영 시스템에서 사용할 수 있는
 * Base62 인코딩된 22자 운영 코드(fasstoProductCode)로 변환한다.
 *
 * <p>이 코드는 Product 생성 시점에 1회 발급되어 영속화되며 이후 불변.
 * Fassto 외부 시스템 연동 호출 시 cstGodCd 페이로드로 사용된다.
 * fulfillment-domain의 FasstoCustomerGoodsCodeEncoder.encode와 동일 알고리즘이며
 * 두 인코더 간 출력 동일성은 FasstoProductCodeGeneratorTest에서 회귀 검증된다.</p>
 */
public final class FasstoProductCodeGenerator {
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);
    private static final int ENCODED_LENGTH = 22;
    private static final int UUID_BYTE_LENGTH = 16;

    private FasstoProductCodeGenerator() {
    }

    public static String encode(UUID productKey) {
        byte[] bytes = uuidToBytes(productKey);
        BigInteger value = new BigInteger(1, bytes);
        return toBase62(value);
    }

    private static byte[] uuidToBytes(UUID productKey) {
        ByteBuffer buffer = ByteBuffer.allocate(UUID_BYTE_LENGTH);
        buffer.putLong(productKey.getMostSignificantBits());
        buffer.putLong(productKey.getLeastSignificantBits());
        return buffer.array();
    }

    private static String toBase62(BigInteger value) {
        StringBuilder builder = new StringBuilder(ENCODED_LENGTH);
        BigInteger remaining = value;
        while (remaining.signum() > 0) {
            int digit = remaining.mod(BASE).intValue();
            builder.append(BASE62_ALPHABET.charAt(digit));
            remaining = remaining.divide(BASE);
        }
        while (builder.length() < ENCODED_LENGTH) {
            builder.append('0');
        }
        return builder.reverse().toString();
    }
}
