package com.personal.marketnote.common.utility;

import com.github.f4b6a3.uuid.UuidCreator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

public class RandomCodeGenerator {
    private static final char[] REFERENCE_CODE_AALLOWED_LETTERS = "ABCDEFGHJKMNPQRTUVWXYZ".toCharArray();
    private static final char[] REFERENCE_CODE_ALLOWED_DIGITS = "2346789".toCharArray();
    private static final char[] ALL_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
    private static final char[] ALL_DIGITS = "1234567890".toCharArray();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateReferenceCode() {
        StringBuilder code = new StringBuilder(6);
        code.append(generateRandomLetter());
        code.append(generateRandomDigit());
        code.append(generateRandomLetter());
        code.append(generateRandomDigit());
        code.append(generateRandomLetter());
        code.append(generateRandomDigit());

        return code.toString();
    }

    private static char generateRandomLetter() {
        return REFERENCE_CODE_AALLOWED_LETTERS[SECURE_RANDOM.nextInt(REFERENCE_CODE_AALLOWED_LETTERS.length)];
    }

    private static char generateRandomDigit() {
        return REFERENCE_CODE_ALLOWED_DIGITS[SECURE_RANDOM.nextInt(REFERENCE_CODE_ALLOWED_DIGITS.length)];
    }

    public static String generateEmailVerificationCode() {
        return generateRandomNumberCode(6);
    }

    public static String generateOrderNumber() {
        return FormatConverter.parseToNumberTime(LocalDateTime.now()) + generateRandomNumberCode(7);
    }

    private static String generateRandomNumberCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALL_DIGITS[SECURE_RANDOM.nextInt(ALL_DIGITS.length)]);
        }

        return code.toString();
    }

    public static UUID generateUserKey() {
        return generateUuidV4();
    }

    private static UUID generateUuidV4() {
        return UUID.randomUUID();
    }

    public static UUID generateOrderKey() {
        return generateUuidV7();
    }

    public static UUID generateOrderProductKey() {
        return generateUuidV7Epoch();
    }

    public static UUID generatePostKey() {
        return generateUuidV7();
    }

    public static UUID generateReviewKey() {
        return generateUuidV7Epoch();
    }

    public static UUID generateProductKey() {
        return generateUuidV7Epoch();
    }

    private static UUID generateUuidV7() {
        return UuidCreator.getTimeOrdered();
    }

    private static UUID generateUuidV7Epoch() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
