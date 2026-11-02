package com.personal.marketnote.common.configuration.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpaqueTokenIntrospectorConfigTest {

    @Test
    @DisplayName("CSP 정책 디렉티브는 default-src 'self'와 frame-ancestors 'none'을 포함한다")
    void shouldDefineCspPolicyDirectivesWithSelfAndFrameAncestorsNone() {
        assertThat(OpaqueTokenIntrospectorConfig.CSP_POLICY_DIRECTIVES)
                .contains("default-src 'self'")
                .contains("frame-ancestors 'none'");
    }
}
