package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserSortProperty;
import com.personal.marketnote.user.port.in.result.GetUserInfoResult;
import com.personal.marketnote.user.port.in.result.GetUserResult;
import com.personal.marketnote.user.port.in.usecase.user.GetUserUseCase;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserService implements GetUserUseCase {
    private final FindUserPort findUserPort;

    @Override
    public boolean existsUser(String referredUserCode) {
        return findUserPort.existsByReferenceCode(referredUserCode);
    }

    @Override
    public GetUserInfoResult getUserInfo(Long id) {
        return GetUserInfoResult.from(getUser(id));
    }

    @Override
    public User getUser(Long id) {
        return findUserPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id)));
    }

    @Override
    public User getUser(AuthVendor authVendor, String oidcId) {
        return findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(USER_OIDC_ID_NOT_FOUND_EXCEPTION_MESSAGE, oidcId)
                ));
    }

    @Override
    public User getUser(String referredUserCode) {
        return findUserPort.findByReferenceCode(referredUserCode)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE, referredUserCode)
                ));
    }

    @Override
    public GetUserInfoResult getAllStatusUserInfo(Long id) {
        return GetUserInfoResult.from(getAllStatusUser(id));
    }

    @Override
    public User getAllStatusUser(Long id) {
        return findUserPort.findAllStatusUserById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id)));
    }

    @Override
    public User getAllStatusUser(String email) throws UserNotFoundException {
        return findUserPort.findAllStatusUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(USER_EMAIL_NOT_FOUND_EXCEPTION_MESSAGE, email))
                );
    }

    @Override
    public User getAllStatusUser(AuthVendor authVendor, String oidcId) {
        return findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format(USER_OIDC_ID_NOT_FOUND_EXCEPTION_MESSAGE, oidcId)
                ));
    }

    @Override
    public UUID getUserKey(Long id) {
        return findUserPort.findUserKeyById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id)));
    }

    @Override
    public Page<GetUserResult> getAllStatusUsers(
            int pageSize, int pageNumber,
            Sort.Direction sortDirection, UserSortProperty sortProperty,
            UserSearchTarget searchTarget, String searchKeyword
    ) {
        Pageable pageable = PageRequest.of(
                pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getCamelCaseValue())
        );

        return findUserPort.findAllStatusUsersByPage(pageable, searchTarget, searchKeyword)
                .map(GetUserResult::from);
    }
}
