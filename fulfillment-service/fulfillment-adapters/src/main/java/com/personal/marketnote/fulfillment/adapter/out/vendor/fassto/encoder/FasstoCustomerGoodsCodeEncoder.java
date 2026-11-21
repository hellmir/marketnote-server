package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.encoder;

import com.personal.marketnote.common.utility.FormatValidator;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.regex.Pattern;

public final class FasstoCustomerGoodsCodeEncoder {
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(62);
    private static final Pattern BASE62_PATTERN = Pattern.compile("^[0-9A-Za-z]+$");
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

    public static UUID decode(String cstGodCd) {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            throw new InvalidFasstoCustomerGoodsCodeLengthException("input is null or blank");
        }
        if (cstGodCd.length() != ENCODED_LENGTH) {
            throw new InvalidFasstoCustomerGoodsCodeLengthException(cstGodCd.length());
        }
        if (!BASE62_PATTERN.matcher(cstGodCd).matches()) {
            throw new InvalidFasstoCustomerGoodsCodeCharsetException();
        }
        BigInteger value = fromBase62(cstGodCd);
        return bytesToUuid(toFixed16Bytes(value));
    }

    public static String decodeCustomerProductCode(String cstGodCd) {
        if (FormatValidator.hasNoValue(cstGodCd)) {
            return cstGodCd;
        }
        if (cstGodCd.length() != ENCODED_LENGTH || !BASE62_PATTERN.matcher(cstGodCd).matches()) {
            return cstGodCd;
        }
        return decode(cstGodCd).toString();
    }

    private static byte[] uuidToBytes(UUID productKey) {
        ByteBuffer buffer = ByteBuffer.allocate(UUID_BYTE_LENGTH);
        buffer.putLong(productKey.getMostSignificantBits());
        buffer.putLong(productKey.getLeastSignificantBits());
        return buffer.array();
    }

    private static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
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

    private static BigInteger fromBase62(String encoded) {
        BigInteger value = BigInteger.ZERO;
        for (int i = 0; i < encoded.length(); i++) {
            int digit = BASE62_ALPHABET.indexOf(encoded.charAt(i));
            value = value.multiply(BASE).add(BigInteger.valueOf(digit));
        }
        return value;
    }

    private static byte[] toFixed16Bytes(BigInteger value) {
        byte[] raw = value.toByteArray();
        if (raw.length == UUID_BYTE_LENGTH) {
            return raw;
        }
        if (raw.length == UUID_BYTE_LENGTH + 1 && raw[0] == 0) {
            byte[] trimmed = new byte[UUID_BYTE_LENGTH];
            System.arraycopy(raw, 1, trimmed, 0, UUID_BYTE_LENGTH);
            return trimmed;
        }
        if (raw.length < UUID_BYTE_LENGTH) {
            byte[] padded = new byte[UUID_BYTE_LENGTH];
            System.arraycopy(raw, 0, padded, UUID_BYTE_LENGTH - raw.length, raw.length);
            return padded;
        }
        throw new InvalidFasstoCustomerGoodsCodeLengthException("decoded byte length=" + raw.length);
    }
}
