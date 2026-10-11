package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AligoTemplateInspectionStatusTest {

    @Test
    @DisplayName("REG에서 REQ로 전이를 허용한다")
    void shouldAllowTransitionFromRegToReq() {
        assertThat(AligoTemplateInspectionStatus.REG.canTransitionTo(AligoTemplateInspectionStatus.REQ)).isTrue();
    }

    @Test
    @DisplayName("REQ에서 APR로 전이를 허용한다")
    void shouldAllowTransitionFromReqToApr() {
        assertThat(AligoTemplateInspectionStatus.REQ.canTransitionTo(AligoTemplateInspectionStatus.APR)).isTrue();
    }

    @Test
    @DisplayName("REQ에서 REJ로 전이를 허용한다")
    void shouldAllowTransitionFromReqToRej() {
        assertThat(AligoTemplateInspectionStatus.REQ.canTransitionTo(AligoTemplateInspectionStatus.REJ)).isTrue();
    }

    @Test
    @DisplayName("REJ에서 REG로 전이를 허용한다")
    void shouldAllowTransitionFromRejToReg() {
        assertThat(AligoTemplateInspectionStatus.REJ.canTransitionTo(AligoTemplateInspectionStatus.REG)).isTrue();
    }

    @Test
    @DisplayName("APR에서 모든 전이를 거부한다")
    void shouldRejectAllTransitionsFromApr() {
        assertThat(AligoTemplateInspectionStatus.APR.canTransitionTo(AligoTemplateInspectionStatus.REG)).isFalse();
        assertThat(AligoTemplateInspectionStatus.APR.canTransitionTo(AligoTemplateInspectionStatus.REQ)).isFalse();
        assertThat(AligoTemplateInspectionStatus.APR.canTransitionTo(AligoTemplateInspectionStatus.REJ)).isFalse();
    }

    @Test
    @DisplayName("REG에서 APR로 직접 전이를 거부한다 (중간 단계 건너뛰기 불가)")
    void shouldRejectDirectTransitionFromRegToApr() {
        assertThat(AligoTemplateInspectionStatus.REG.canTransitionTo(AligoTemplateInspectionStatus.APR)).isFalse();
    }
}
