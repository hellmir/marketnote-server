package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class KcpSignatureGenerator {
    private final KcpCertificateLoader kcpCertificateLoader;

    public String generateSignData(String siteCd, String tno, String modType) {
        PrivateKey privateKey = kcpCertificateLoader.loadPrivateKey();
        String plainText = siteCd + "^" + tno + "^" + modType;

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] signedBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            throw new IllegalStateException("KCP 서명 생성 실패: tno=" + tno, e);
        }
    }
}
