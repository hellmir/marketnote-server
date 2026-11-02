package com.personal.marketnote.user.service.terms;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.port.in.result.GetTermsResult;
import com.personal.marketnote.user.port.in.result.GetUserTermsResult;
import com.personal.marketnote.user.port.in.usecase.terms.GetTermsUseCase;
import com.personal.marketnote.user.port.out.user.FindTermsPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetTermsService implements GetTermsUseCase {
    private final FindTermsPort findTermsPort;
    private final FindUserPort findUserPort;

    @Override
    public GetTermsResult getAllTerms() {
        return GetTermsResult.from(findTermsPort.findAll());
    }

    @Override
    public GetUserTermsResult getUserTerms(Long userId) {
        return GetUserTermsResult.from(
                findUserPort.findById(userId)
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, userId)
                                )
                        )
                        .getUserTerms()
        );
    }
}
