package com.personal.marketnote.user.service.terms;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.port.in.command.AcceptOrCancelTermsCommand;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.in.usecase.terms.UpdateTermsUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, timeout = 120)
public class UpdateTermsService implements UpdateTermsUseCase {
    private final FindUserPort findUserPort;
    private final UpdateUserPort updateUserPort;

    @Override
    public GetUserTermsResult acceptOrCancelTerms(Long userId, AcceptOrCancelTermsCommand acceptOrCancelTermsCommand) {
        User user = findUserPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, userId)));
        user.acceptOrCancelTerms(acceptOrCancelTermsCommand.getIds());
        updateUserPort.update(user);

        return GetUserTermsResult.from(user.getUserTerms());
    }
}
