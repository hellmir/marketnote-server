package com.personal.marketnote.reward.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateOfferwallRewardExceptionTest {

    @Test
    @DisplayName("기본 생성자는 cause 없이 메시지를 보존한다")
    void preservesMessageWithoutCause() {
        DuplicateOfferwallRewardException ex = new DuplicateOfferwallRewardException();

        assertThat(ex.getMessage()).contains("이미 해당 캠페인에 대한 리워드가 지급되었습니다.");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    @DisplayName("cause 인자 생성자는 cause를 보존한다")
    void preservesCauseWhenProvided() {
        RuntimeException root = new RuntimeException("ux_offerwall_success constraint violation");

        DuplicateOfferwallRewardException ex = new DuplicateOfferwallRewardException(root);

        assertThat(ex.getMessage()).contains("이미 해당 캠페인에 대한 리워드가 지급되었습니다.");
        assertThat(ex.getCause()).isSameAs(root);
    }
}
