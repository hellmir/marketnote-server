package com.personal.marketnote.common.domain.exception.token;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VendorVerificationFailedExceptionTest {

    @Test
    @DisplayName("message 단일 인자 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        VendorVerificationFailedException ex = new VendorVerificationFailedException("invalid signature");

        assertThat(ex.getMessage()).isEqualTo("invalid signature");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("message와 cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        Exception root = new Exception("HmacMD5 init failed");

        VendorVerificationFailedException ex = new VendorVerificationFailedException("invalid signature", root);

        assertThat(ex.getMessage()).isEqualTo("invalid signature");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
