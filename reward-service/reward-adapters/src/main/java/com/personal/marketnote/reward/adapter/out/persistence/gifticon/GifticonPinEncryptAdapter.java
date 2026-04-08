package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.configuration.GifticonPinProperties;
import com.personal.marketnote.reward.domain.exception.GifticonPinEncryptionFailedException;
import com.personal.marketnote.reward.port.out.gifticon.EncryptGifticonPinPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class GifticonPinEncryptAdapter implements EncryptGifticonPinPort {

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public GifticonPinEncryptAdapter(GifticonPinProperties properties) {
        this.keySpec = new SecretKeySpec(
                properties.getEncryptKey().getBytes(StandardCharsets.UTF_8), "AES"
        );
    }

    @Override
    public String encrypt(String plainPin) {
        try {
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainPin.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("PIN 암호화 실패: error={}", e.getMessage(), e);
            throw new GifticonPinEncryptionFailedException(e);
        }
    }
}
