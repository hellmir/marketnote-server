package com.personal.marketnote.commerce.adapter.out.vendor.kcp;

import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.InvalidKcpCertificatePathException;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCertificateLoadFailedException;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpCertificatePathNotConfiguredException;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpPrivateKeyLoadFailedException;
import com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception.KcpPrivateKeyPathNotConfiguredException;
import com.personal.marketnote.commerce.configuration.KcpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KcpCertificateLoader 단위 테스트")
class KcpCertificateLoaderTest {

    @TempDir
    Path tempDir;

    private KcpProperties properties;
    private KcpCertificateLoader loader;

    @BeforeEach
    void setUp() {
        properties = new KcpProperties();
        loader = new KcpCertificateLoader(properties);
    }

    @Test
    @DisplayName("certInfoPath가 미설정이면 KcpCertificatePathNotConfiguredException을 던진다")
    void shouldThrowWhenCertPathNotConfigured() {
        // when & then
        assertThatThrownBy(() -> loader.loadCertInfo())
                .isInstanceOf(KcpCertificatePathNotConfiguredException.class);
    }

    @Test
    @DisplayName("파일 시스템 경로의 인증서 내용을 읽어 반환한다")
    void shouldLoadCertInfoFromFileSystem() throws IOException {
        // given
        Path certFile = tempDir.resolve("cert.pem");
        Files.writeString(certFile, "CERT_CONTENT");
        properties.setCertInfoPath(certFile.toString());

        // when
        String result = loader.loadCertInfo();

        // then
        assertThat(result).isEqualTo("CERT_CONTENT");
    }

    @Test
    @DisplayName("경로에 .. 가 포함되면 InvalidKcpCertificatePathException을 던진다")
    void shouldRejectPathTraversal() {
        // given
        properties.setCertInfoPath("/tmp/../etc/passwd");

        // when & then
        assertThatThrownBy(() -> loader.loadCertInfo())
                .isInstanceOf(InvalidKcpCertificatePathException.class);
    }

    @Test
    @DisplayName("존재하지 않는 인증서 경로면 KcpCertificateLoadFailedException을 던진다")
    void shouldThrowWhenCertFileMissing() {
        // given
        properties.setCertInfoPath(tempDir.resolve("missing.pem").toString());

        // when & then
        assertThatThrownBy(() -> loader.loadCertInfo())
                .isInstanceOf(KcpCertificateLoadFailedException.class);
    }

    @Test
    @DisplayName("privateKeyPath가 미설정이면 KcpPrivateKeyPathNotConfiguredException을 던진다")
    void shouldThrowWhenPrivateKeyPathNotConfigured() {
        // when & then
        assertThatThrownBy(() -> loader.loadPrivateKey())
                .isInstanceOf(KcpPrivateKeyPathNotConfiguredException.class);
    }

    @Test
    @DisplayName("PEM 형식의 RSA 개인키를 로드한다")
    void shouldLoadPrivateKey() throws Exception {
        // given
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";

        Path keyFile = tempDir.resolve("private.pem");
        Files.writeString(keyFile, pem);
        properties.setPrivateKeyPath(keyFile.toString());

        // when
        PrivateKey privateKey = loader.loadPrivateKey();

        // then
        assertThat(privateKey).isNotNull();
        assertThat(privateKey.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    @DisplayName("개인키 파일이 잘못된 형식이면 KcpPrivateKeyLoadFailedException을 던진다")
    void shouldThrowWhenPrivateKeyInvalid() throws IOException {
        // given
        Path keyFile = tempDir.resolve("invalid.pem");
        Files.writeString(keyFile, "invalid-content");
        properties.setPrivateKeyPath(keyFile.toString());

        // when & then
        assertThatThrownBy(() -> loader.loadPrivateKey())
                .isInstanceOf(KcpPrivateKeyLoadFailedException.class);
    }
}
