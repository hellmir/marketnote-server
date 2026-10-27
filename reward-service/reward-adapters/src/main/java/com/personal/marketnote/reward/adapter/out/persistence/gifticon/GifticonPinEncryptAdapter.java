package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.configuration.GifticonPinProperties;
import com.personal.marketnote.reward.domain.exception.GifticonPinDecryptionFailedException;
import com.personal.marketnote.reward.domain.exception.GifticonPinEncryptKeyNoValueException;
import com.personal.marketnote.reward.domain.exception.GifticonPinEncryptionFailedException;
import com.personal.marketnote.reward.domain.exception.InvalidGifticonPinEncryptKeyException;
import com.personal.marketnote.reward.port.out.gifticon.DecryptGifticonPinPort;
import com.personal.marketnote.reward.port.out.gifticon.EncryptGifticonPinPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
@Slf4j
public class GifticonPinEncryptAdapter implements EncryptGifticonPinPort, DecryptGifticonPinPort {

    private static final byte[] GCM_MAGIC = "GCM1".getBytes(StandardCharsets.US_ASCII);
    private static final int GCM_MAGIC_LENGTH = 4;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BIT_LENGTH = 128;
    private static final int CBC_IV_LENGTH = 16;
    private static final String GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public GifticonPinEncryptAdapter(GifticonPinProperties properties) {
        byte[] keyBytes = validateAndResolveKey(properties.getEncryptKey());
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    private static byte[] validateAndResolveKey(String encryptKey) {
        if (FormatValidator.hasNoValue(encryptKey)) {
            throw new GifticonPinEncryptKeyNoValueException();
        }
        byte[] keyBytes = encryptKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new InvalidGifticonPinEncryptKeyException(keyBytes.length);
        }
        return keyBytes;
    }

    @Override
    public String encrypt(String plainPin) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] ciphertext = cipher.doFinal(plainPin.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[GCM_MAGIC_LENGTH + iv.length + ciphertext.length];
            System.arraycopy(GCM_MAGIC, 0, combined, 0, GCM_MAGIC_LENGTH);
            System.arraycopy(iv, 0, combined, GCM_MAGIC_LENGTH, iv.length);
            System.arraycopy(ciphertext, 0, combined, GCM_MAGIC_LENGTH + iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("PIN 암호화 실패: error={}", e.getMessage(), e);
            throw new GifticonPinEncryptionFailedException(e);
        }
    }

    @Override
    public String decrypt(String encryptedPin) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedPin);
            if (hasGcmMagic(decoded)) {
                return decryptGcm(decoded);
            }
            return decryptLegacyCbc(decoded);
        } catch (GifticonPinDecryptionFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("PIN 복호화 실패: error={}", e.getMessage(), e);
            throw new GifticonPinDecryptionFailedException(e);
        }
    }

    private boolean hasGcmMagic(byte[] decoded) {
        if (decoded.length < GCM_MAGIC_LENGTH) {
            return false;
        }
        for (int i = 0; i < GCM_MAGIC_LENGTH; i++) {
            if (decoded[i] != GCM_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private String decryptGcm(byte[] decoded) {
        try {
            byte[] iv = Arrays.copyOfRange(decoded, GCM_MAGIC_LENGTH, GCM_MAGIC_LENGTH + GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(decoded, GCM_MAGIC_LENGTH + GCM_IV_LENGTH, decoded.length);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(GCM_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] decrypted = cipher.doFinal(ciphertext);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("PIN 복호화 실패(GCM): error={}", e.getMessage(), e);
            throw new GifticonPinDecryptionFailedException(e);
        }
    }

    private String decryptLegacyCbc(byte[] decoded) {
        try {
            byte[] iv = Arrays.copyOfRange(decoded, 0, CBC_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(decoded, CBC_IV_LENGTH, decoded.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(CBC_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(ciphertext);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("PIN 복호화 실패(CBC 레거시): error={}", e.getMessage(), e);
            throw new GifticonPinDecryptionFailedException(e);
        }
    }
}
