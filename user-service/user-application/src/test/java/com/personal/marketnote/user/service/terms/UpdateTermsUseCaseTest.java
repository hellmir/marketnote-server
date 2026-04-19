package com.personal.marketnote.user.service.terms;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserTerms;
import com.personal.marketnote.user.port.in.command.AcceptOrCancelTermsCommand;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTermsUseCaseTest {
    @Mock
    private FindUserPort findUserPort;
    @Mock
    private UpdateUserPort updateUserPort;

    @InjectMocks
    private UpdateTermsService updateTermsService;

    @Test
    @DisplayName("이용 약관 동의/철회 요청 시 회원 약관 상태를 갱신한다")
    void acceptOrCancelTerms_success_updatesUserTerms() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        Terms terms1 = TermsTestObjectFactory.createTerms(
                1L,
                "required-terms",
                true,
                LocalDateTime.of(2024, 5, 1, 0, 0),
                LocalDateTime.of(2024, 5, 2, 0, 0),
                EntityStatus.ACTIVE
        );
        Terms terms2 = TermsTestObjectFactory.createTerms(
                2L,
                "optional-terms",
                false,
                LocalDateTime.of(2024, 6, 1, 0, 0),
                LocalDateTime.of(2024, 6, 2, 0, 0),
                EntityStatus.ACTIVE
        );
        UserTerms userTerms1 = TermsTestObjectFactory.createUserTerms(
                user,
                terms1,
                true,
                LocalDateTime.of(2024, 5, 1, 0, 0),
                LocalDateTime.of(2024, 5, 2, 0, 0)
        );
        UserTerms userTerms2 = TermsTestObjectFactory.createUserTerms(
                user,
                terms2,
                false,
                LocalDateTime.of(2024, 6, 1, 0, 0),
                LocalDateTime.of(2024, 6, 2, 0, 0)
        );
        AcceptOrCancelTermsCommand command = AcceptOrCancelTermsCommand.of(List.of(1L, 2L));

        when(findUserPort.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserTerms()).thenReturn(List.of(userTerms1, userTerms2));

        // when
        GetUserTermsResult result = updateTermsService.acceptOrCancelTerms(userId, command);

        // then
        assertThat(result.userTerms()).hasSize(2);
        assertThat(result.userTerms().get(0).id()).isEqualTo(1L);
        assertThat(result.userTerms().get(0).content()).isEqualTo("required-terms");
        assertThat(result.userTerms().get(0).isRequired()).isTrue();
        assertThat(result.userTerms().get(0).isAgreed()).isTrue();
        assertThat(result.userTerms().get(1).id()).isEqualTo(2L);
        assertThat(result.userTerms().get(1).content()).isEqualTo("optional-terms");
        assertThat(result.userTerms().get(1).isRequired()).isFalse();
        assertThat(result.userTerms().get(1).isAgreed()).isFalse();

        verify(findUserPort).findById(userId);
        verify(user).acceptOrCancelTerms(command.getIds());
        verify(updateUserPort).update(user);
        verify(user).getUserTerms();
        verifyNoMoreInteractions(findUserPort, updateUserPort, user);
    }

    @Test
    @DisplayName("약관 동의 여부 업데이트 대상 회원이 존재하지 않으면 예외를 던진다")
    void acceptOrCancelTerms_userNotFound_throws() {
        // given
        Long userId = 2L;
        AcceptOrCancelTermsCommand command = AcceptOrCancelTermsCommand.of(List.of(1L));

        when(findUserPort.findById(userId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> updateTermsService.acceptOrCancelTerms(userId, command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, userId));

        verify(findUserPort).findById(userId);
        verifyNoMoreInteractions(findUserPort);
        verifyNoInteractions(updateUserPort);
    }
}
