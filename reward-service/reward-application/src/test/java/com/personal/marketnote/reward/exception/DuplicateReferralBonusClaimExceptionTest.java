package com.personal.marketnote.reward.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateReferralBonusClaimExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateReferralBonusClaimException ex = new DuplicateReferralBonusClaimException();

        assertThat(ex.getMessage()).contains("ERR_REFERRAL_02");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("duplicate user point history");

        DuplicateReferralBonusClaimException ex = new DuplicateReferralBonusClaimException(root);

        assertThat(ex.getMessage()).contains("ERR_REFERRAL_02");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
