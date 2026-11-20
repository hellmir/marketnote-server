package com.personal.marketnote.common.configuration.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class OpaqueTokenIntrospectorConfigTest {

    private static final String DEPRECATED_BEARER_FILTER_FQCN_INTERNAL =
            "org/springframework/security/oauth2/server/resource/web/BearerTokenAuthenticationFilter";
    private static final String CURRENT_BEARER_FILTER_FQCN_INTERNAL =
            "org/springframework/security/oauth2/server/resource/web/authentication/BearerTokenAuthenticationFilter";

    @Test
    @DisplayName("CSP 정책 디렉티브는 default-src 'self'와 frame-ancestors 'none'을 포함한다")
    void shouldDefineCspPolicyDirectivesWithSelfAndFrameAncestorsNone() {
        assertThat(OpaqueTokenIntrospectorConfig.CSP_POLICY_DIRECTIVES)
                .contains("default-src 'self'")
                .contains("frame-ancestors 'none'");
    }

    @Test
    @DisplayName("addFilterBefore 앵커는 deprecated 패키지가 아닌 신규 BearerTokenAuthenticationFilter 패키지를 참조한다")
    void shouldReferenceCurrentBearerTokenAuthenticationFilterPackageOnly() throws IOException {
        String classFileBytes = readClassFileAsLatin1(OpaqueTokenIntrospectorConfig.class);

        assertThat(classFileBytes)
                .as("신규 패키지 (.web.authentication.BearerTokenAuthenticationFilter) 참조가 존재해야 한다")
                .contains(CURRENT_BEARER_FILTER_FQCN_INTERNAL);
        // 신규 FQCN 은 deprecated FQCN 의 supersequence 이므로 단순 substring 검사가 불가능하다.
        // 신규 FQCN 모두 마스킹한 잔여 바이트에 deprecated FQCN 이 부재해야 한다 — CONSTANT_Class/Utf8/시그니처 표현 모두 검출.
        String masked = classFileBytes.replace(CURRENT_BEARER_FILTER_FQCN_INTERNAL, "");
        assertThat(masked)
                .as("deprecated 패키지 (.web.BearerTokenAuthenticationFilter) 참조가 남아 있으면 안 된다")
                .doesNotContain(DEPRECATED_BEARER_FILTER_FQCN_INTERNAL);
    }

    private String readClassFileAsLatin1(Class<?> targetClass) throws IOException {
        String resourcePath = "/" + targetClass.getName().replace('.', '/') + ".class";
        try (InputStream input = targetClass.getResourceAsStream(resourcePath)) {
            assertThat(input).as("클래스 리소스를 찾을 수 있어야 한다").isNotNull();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int read;
            while ((read = input.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString(StandardCharsets.ISO_8859_1);
        }
    }
}
