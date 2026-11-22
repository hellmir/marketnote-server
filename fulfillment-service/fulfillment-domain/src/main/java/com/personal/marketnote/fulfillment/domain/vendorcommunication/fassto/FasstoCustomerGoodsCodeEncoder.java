package com.personal.marketnote.fulfillment.domain.vendorcommunication.fassto;

import com.personal.marketnote.common.utility.FormatValidator;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public final class FasstoCustomerGoodsCodeEncoder {
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);
    private static final int ENCODED_LENGTH = 22;
    private static final int UUID_BYTE_LENGTH = 16;

    private FasstoCustomerGoodsCodeEncoder() {
    }

    public static String encode(UUID productKey) {
        byte[] bytes = uuidToBytes(productKey);
        BigInteger value = new BigInteger(1, bytes);
        return toBase62(value);
    }

    public static String encodeOrFallback(UUID productKey, String fallbackCode) {
        if (FormatValidator.hasValue(productKey)) {
            return encode(productKey);
        }
        return fallbackCode;
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
