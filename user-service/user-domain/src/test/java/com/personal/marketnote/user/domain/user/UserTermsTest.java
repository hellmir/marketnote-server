package com.personal.marketnote.user.domain.user;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTermsTest {

    @Test
    @DisplayName("UserTermsCreateState로 UserTerms를 생성하면 모든 필드가 매핑된다")
    void shouldCreateUserTermsFromCreateState() {
        Terms terms = createRequiredTerms();
        UserTermsCreateState state = UserTermsCreateState.builder()
                .user(null)
                .terms(terms)
                .agreementYn(true)
                .build();

        UserTerms userTerms = UserTerms.from(state);

        assertThat(userTerms.getTerms()).isEqualTo(terms);
        assertThat(userTerms.getAgreementYn()).isTrue();
    }

    @Test
    @DisplayName("동의 상태에서 acceptOrCancel을 호출하면 비동의로 전환된다")
    void shouldToggleFromAgreedToDisagreed() {
        UserTerms userTerms = createUserTerms(true);

        userTerms.acceptOrCancel();

        assertThat(userTerms.getAgreementYn()).isFalse();
    }

    @Test
    @DisplayName("비동의 상태에서 acceptOrCancel을 호출하면 동의로 전환된다")
    void shouldToggleFromDisagreedToAgreed() {
        UserTerms userTerms = createUserTerms(false);

        userTerms.acceptOrCancel();

        assertThat(userTerms.getAgreementYn()).isTrue();
    }

    @Test
    @DisplayName("필수 약관에 동의하면 isRequiredTermsAgreed는 true를 반환한다")
    void shouldReturnTrueWhenRequiredTermsAgreed() {
        UserTerms userTerms = createUserTermsWithRequiredTerms(true, true);

        assertThat(userTerms.isRequiredTermsAgreed()).isTrue();
    }

    @Test
    @DisplayName("필수 약관에 비동의하면 isRequiredTermsAgreed는 false를 반환한다")
    void shouldReturnFalseWhenRequiredTermsNotAgreed() {
        UserTerms userTerms = createUserTermsWithRequiredTerms(true, false);

        assertThat(userTerms.isRequiredTermsAgreed()).isFalse();
    }

    @Test
    @DisplayName("선택 약관에 비동의해도 isRequiredTermsAgreed는 true를 반환한다")
    void shouldReturnTrueWhenOptionalTermsNotAgreed() {
        UserTerms userTerms = createUserTermsWithRequiredTerms(false, false);

        assertThat(userTerms.isRequiredTermsAgreed()).isTrue();
    }

    @Test
    @DisplayName("disagree를 호출하면 동의 상태가 false로 변경된다")
    void shouldSetAgreementToFalseWhenDisagree() {
        UserTerms userTerms = createUserTerms(true);

        userTerms.disagree();

        assertThat(userTerms.getAgreementYn()).isFalse();
    }

    private UserTerms createUserTerms(boolean agreementYn) {
        return UserTerms.from(UserTermsCreateState.builder()
                .user(null)
                .terms(createRequiredTerms())
                .agreementYn(agreementYn)
                .build());
    }

    private UserTerms createUserTermsWithRequiredTerms(boolean requiredYn, boolean agreementYn) {
        Terms terms = Terms.from(TermsSnapshotState.builder()
                .id(1L)
                .content("약관 내용")
                .requiredYn(requiredYn)
                .status(EntityStatus.ACTIVE)
                .build());
        return UserTerms.from(UserTermsCreateState.builder()
                .user(null)
                .terms(terms)
                .agreementYn(agreementYn)
                .build());
    }

    private Terms createRequiredTerms() {
        return Terms.from(TermsSnapshotState.builder()
                .id(1L)
                .content("필수 약관")
                .requiredYn(true)
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
