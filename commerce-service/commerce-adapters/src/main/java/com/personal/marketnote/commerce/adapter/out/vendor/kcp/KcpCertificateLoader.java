package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.personal.marketnote.commerce.configuration.KcpProperties;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class KcpCertificateLoader {
    private static final String CLASSPATH_PREFIX = "classpath:";

    private final KcpProperties kcpProperties;

    public String loadCertInfo() {
        String certInfoPath = kcpProperties.getCertInfoPath();
        if (FormatValidator.hasNoValue(certInfoPath)) {
            throw new IllegalStateException("KCP 인증서 경로(kcp.cert-info-path)가 설정되지 않았습니다");
        }

        try {
            return readContent(certInfoPath);
        } catch (IOException e) {
            throw new IllegalStateException("KCP 인증서 파일 로드 실패: " + certInfoPath, e);
        }
    }

    public PrivateKey loadPrivateKey() {
        String privateKeyPath = kcpProperties.getPrivateKeyPath();
        if (FormatValidator.hasNoValue(privateKeyPath)) {
            throw new IllegalStateException("KCP 개인키 경로(kcp.private-key-path)가 설정되지 않았습니다");
        }

        try {
            String keyContent = readContent(privateKeyPath);
            String keyBase64 = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("KCP 개인키 파일 로드 실패: " + privateKeyPath, e);
        }
    }

    private String readContent(String path) throws IOException {
        if (path.contains("..")) {
            throw new IllegalStateException("잘못된 인증서 경로입니다: " + path);
        }

        if (path.startsWith(CLASSPATH_PREFIX)) {
            String resourcePath = path.substring(CLASSPATH_PREFIX.length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        }

        return Files.readString(Path.of(path)).trim();
    }
}
