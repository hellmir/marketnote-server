package com.personal.marketnote.user.service.terms;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.Terms;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserTerms;
import com.personal.marketnote.user.port.in.result.GetTermsResult;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.out.user.FindTermsPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
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
class GetTermsUseCaseTest {
    @Mock
    private FindTermsPort findTermsPort;
    @Mock
    private FindUserPort findUserPort;

    @InjectMocks
    private GetTermsService getTermsService;

    @Test
    @DisplayName("전체 이용 약관 목록을 조회한다")
    void getAllTerms_success_mapsResults() {
        // given
        Terms terms1 = TermsTestObjectFactory.createTerms(
                1L,
                "terms-1",
                true,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                EntityStatus.ACTIVE
        );
        Terms terms2 = TermsTestObjectFactory.createTerms(
                2L,
                "terms-2",
                false,
                LocalDateTime.of(2024, 2, 1, 0, 0),
                LocalDateTime.of(2024, 2, 2, 0, 0),
                EntityStatus.ACTIVE
        );

        when(findTermsPort.findAll()).thenReturn(List.of(terms1, terms2));

        // when
        GetTermsResult result = getTermsService.getAllTerms();

        // then
        assertThat(result.getTermResults()).hasSize(2);
        assertThat(result.getTermResults().get(0).id()).isEqualTo(1L);
        assertThat(result.getTermResults().get(0).content()).isEqualTo("terms-1");
        assertThat(result.getTermResults().get(0).isRequired()).isTrue();
        assertThat(result.getTermResults().get(1).id()).isEqualTo(2L);
        assertThat(result.getTermResults().get(1).content()).isEqualTo("terms-2");
        assertThat(result.getTermResults().get(1).isRequired()).isFalse();

        verify(findTermsPort).findAll();
        verifyNoMoreInteractions(findTermsPort, findUserPort);
    }

    @Test
    @DisplayName("이용 약관이 없으면 빈 리스트를 반환한다")
    void getAllTerms_empty_returnsEmptyResult() {
        // given
        when(findTermsPort.findAll()).thenReturn(List.of());

        // when
        GetTermsResult result = getTermsService.getAllTerms();

        // then
        assertThat(result.getTermResults()).isEmpty();
        verify(findTermsPort).findAll();
        verifyNoMoreInteractions(findTermsPort, findUserPort);
    }

    @Test
    @DisplayName("회원 이용 약관 동의 여부 목록을 조회한다")
    void getUserTerms_success_mapsResults() {
        // given
        Long userId = 1L;
        User user = mock(User.class);
        Terms terms1 = TermsTestObjectFactory.createTerms(
                1L,
                "required-terms",
                true,
                LocalDateTime.of(2024, 3, 1, 0, 0),
                LocalDateTime.of(2024, 3, 2, 0, 0),
                EntityStatus.ACTIVE
        );
        Terms terms2 = TermsTestObjectFactory.createTerms(
                2L,
                "optional-terms",
                false,
                LocalDateTime.of(2024, 4, 1, 0, 0),
                LocalDateTime.of(2024, 4, 2, 0, 0),
                EntityStatus.ACTIVE
        );
        UserTerms userTerms1 = TermsTestObjectFactory.createUserTerms(
                user,
                terms1,
                true,
                LocalDateTime.of(2024, 3, 1, 0, 0),
                LocalDateTime.of(2024, 3, 2, 0, 0)
        );
        UserTerms userTerms2 = TermsTestObjectFactory.createUserTerms(
                user,
                terms2,
                false,
                LocalDateTime.of(2024, 4, 1, 0, 0),
                LocalDateTime.of(2024, 4, 2, 0, 0)
        );

        when(findUserPort.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserTerms()).thenReturn(List.of(userTerms1, userTerms2));

        // when
        GetUserTermsResult result = getTermsService.getUserTerms(userId);

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
        verify(user).getUserTerms();
        verifyNoMoreInteractions(findUserPort, findTermsPort, user);
    }

    @Test
    @DisplayName("회원 이용 약관 동의 목록이 없으면 빈 리스트를 반환한다")
    void getUserTerms_empty_returnsEmptyResult() {
        // given
        Long userId = 2L;
        User user = mock(User.class);

        when(findUserPort.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUserTerms()).thenReturn(List.of());

        // when
        GetUserTermsResult result = getTermsService.getUserTerms(userId);

        // then
        assertThat(result.userTerms()).isEmpty();
        verify(findUserPort).findById(userId);
        verify(user).getUserTerms();
        verifyNoMoreInteractions(findUserPort, findTermsPort, user);
    }

    @Test
    @DisplayName("회원 이용 약관 동의 목록 조회 대상 회원이 존재하지 않으면 예외를 던진다")
    void getUserTerms_userNotFound_throws() {
        // given
        Long userId = 3L;
        when(findUserPort.findById(userId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getTermsService.getUserTerms(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, userId));

        verify(findUserPort).findById(userId);
        verifyNoMoreInteractions(findUserPort, findTermsPort);
    }
}
