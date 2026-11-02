package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.domain.email.Email;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.exception.UserExistsException;
import com.personal.marketnote.user.port.in.usecase.user.RegisterEmailUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.FIRST_ERROR_CODE;
import static com.personal.marketnote.user.exception.ExceptionMessage.EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE;
import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@RequiredArgsConstructor
@UseCase
@Transactional(isolation = READ_COMMITTED)
public class RegisterEmailService implements RegisterEmailUseCase {
    private final FindUserPort findUserPort;
    private final UpdateUserPort updateUserPort;

    @Override
    public void registerEmail(Long id, AuthVendor authVendor, String email) {
        User user = findUserPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id)));

        Email newEmail = Email.of(email);

        if (findUserPort.existsByEmail(newEmail.getValue())) {
            throw new UserExistsException(String.format(EMAIL_ALREADY_EXISTS_EXCEPTION_MESSAGE, FIRST_ERROR_CODE, newEmail.getValue()));
        }

        user.registerEmail(newEmail);
        updateUserPort.update(user);
    }
}
