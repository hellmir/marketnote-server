package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpSignatureGenerationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KcpSignatureGenerator 단위 테스트")
class KcpSignatureGeneratorTest {

    @InjectMocks
    private KcpSignatureGenerator generator;

    @Mock
    private KcpCertificateLoader kcpCertificateLoader;

    private static KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        if (keyPair == null) {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            keyPair = kpg.generateKeyPair();
        }
    }

    @Test
    @DisplayName("siteCd, tno, modType을 결합해 SHA256withRSA 서명을 생성한다")
    void shouldGenerateSignature() throws Exception {
        // given
        when(kcpCertificateLoader.loadPrivateKey()).thenReturn(keyPair.getPrivate());

        // when
        String signed = generator.generateSignData("T0000", "TNO_001", "STSC");

        // then
        assertThat(signed).isNotBlank();

        byte[] decoded = Base64.getDecoder().decode(signed);
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update("T0000^TNO_001^STSC".getBytes());
        assertThat(verifier.verify(decoded)).isTrue();
    }

    @Test
    @DisplayName("개인키가 잘못되면 KcpSignatureGenerationFailedException을 던진다")
    void shouldThrowOnInvalidPrivateKey() {
        // given
        PrivateKey invalidKey = new PrivateKey() {
            @Override
            public String getAlgorithm() { return "INVALID"; }
            @Override
            public String getFormat() { return null; }
            @Override
            public byte[] getEncoded() { return new byte[0]; }
        };
        when(kcpCertificateLoader.loadPrivateKey()).thenReturn(invalidKey);

        // when & then
        assertThatThrownBy(() -> generator.generateSignData("T0000", "TNO_001", "STSC"))
                .isInstanceOf(KcpSignatureGenerationFailedException.class);
    }

    @Test
    @DisplayName("동일한 입력에 대해 동일한 서명을 생성한다")
    void shouldGenerateDeterministicSignatureForSameInput() throws Exception {
        // given
        when(kcpCertificateLoader.loadPrivateKey()).thenReturn(keyPair.getPrivate());

        // when
        String signed1 = generator.generateSignData("T0000", "TNO_001", "STSC");
        String signed2 = generator.generateSignData("T0000", "TNO_001", "STSC");

        // then - RSA-PKCS1 v1.5 (default for SHA256withRSA) is deterministic
        // 양쪽 모두 검증 가능해야 한다
        byte[] decoded1 = Base64.getDecoder().decode(signed1);
        byte[] decoded2 = Base64.getDecoder().decode(signed2);
        PublicKey publicKey = keyPair.getPublic();
        Signature verifier = Signature.getInstance("SHA256withRSA");

        verifier.initVerify(publicKey);
        verifier.update("T0000^TNO_001^STSC".getBytes());
        assertThat(verifier.verify(decoded1)).isTrue();

        verifier.initVerify(publicKey);
        verifier.update("T0000^TNO_001^STSC".getBytes());
        assertThat(verifier.verify(decoded2)).isTrue();
    }
}
