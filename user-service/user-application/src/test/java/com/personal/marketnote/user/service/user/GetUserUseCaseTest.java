package com.personal.marketnote.user.service.user;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.domain.authentication.Role;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserAuthProvider;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserSortProperty;
import com.personal.marketnote.user.port.in.result.AccountResult;
import com.personal.marketnote.user.port.in.result.GetUserInfoResult;
import com.personal.marketnote.user.port.in.result.GetUserResult;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.personal.marketnote.user.exception.ExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {
    @Mock
    private FindUserPort findUserPort;

    @InjectMocks
    private GetUserService getUserService;

    @Test
    @DisplayName("회원 ID를 전송해 회원 정보 결과를 조회한다")
    void getUserInfo_success_getResult() {
        // given
        Long id = 1L;
        String nickname = "tester";
        String email = "user@test.com";
        String fullName = "홍길동";
        String phoneNumber = "010-1111-2222";
        String referenceCode = "ref-123";
        Role role = Role.getBuyer();
        List<UserAuthProvider> userAuthProviders = List.of(
                UserAuthProvider.of(AuthVendor.KAKAO, "kakao-oidc"),
                UserAuthProvider.of(AuthVendor.GOOGLE, "google-oidc")
        );
        LocalDateTime signedUpAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime lastLoggedInAt = LocalDateTime.of(2024, 1, 2, 11, 0);
        EntityStatus status = EntityStatus.ACTIVE;
        boolean withdrawalYn = false;
        Long orderNum = 7L;

        User user = UserTestObjectFactory.createUser(
                id,
                nickname,
                email,
                fullName,
                phoneNumber,
                referenceCode,
                role,
                userAuthProviders,
                signedUpAt,
                lastLoggedInAt,
                status,
                withdrawalYn,
                orderNum
        );

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));

        // when
        GetUserInfoResult result = getUserService.getUserInfo(id);

        // then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.fullName()).isEqualTo(fullName);
        assertThat(result.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.referenceCode()).isEqualTo(referenceCode);
        assertThat(result.roleId()).isEqualTo(role.getId());
        assertThat(result.signedUpAt()).isEqualTo(signedUpAt);
        assertThat(result.lastLoggedInAt()).isEqualTo(lastLoggedInAt);
        assertThat(result.status()).isEqualTo(status.name());
        assertThat(result.isWithdrawn()).isFalse();
        assertThat(result.orderNum()).isEqualTo(orderNum);
        assertThat(result.accountInfo().accounts()).containsExactly(
                new AccountResult(AuthVendor.KAKAO, "kakao-oidc"),
                new AccountResult(AuthVendor.GOOGLE, "google-oidc")
        );

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회하면 예외를 던진다")
    void getUserInfo_notFound_throws() {
        // given
        Long id = 999L;
        when(findUserPort.findById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getUserInfo(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID를 전송해 전체 회원 정보 결과를 조회한다")
    void getAllStatusUserInfo_success_getResult() {
        // given
        Long id = 4L;
        String nickname = "tester2";
        String email = "user2@test.com";
        String fullName = "김영희";
        String phoneNumber = "010-2222-3333";
        String referenceCode = "ref-456";
        Role role = Role.getBuyer();
        List<UserAuthProvider> userAuthProviders = List.of(
                UserAuthProvider.of(AuthVendor.KAKAO, "kakao-oidc-2")
        );
        LocalDateTime signedUpAt = LocalDateTime.of(2024, 2, 1, 9, 0);
        LocalDateTime lastLoggedInAt = LocalDateTime.of(2024, 2, 2, 10, 0);
        EntityStatus status = EntityStatus.INACTIVE;
        boolean withdrawalYn = true;
        Long orderNum = 3L;

        User user = UserTestObjectFactory.createUser(
                id,
                nickname,
                email,
                fullName,
                phoneNumber,
                referenceCode,
                role,
                userAuthProviders,
                signedUpAt,
                lastLoggedInAt,
                status,
                withdrawalYn,
                orderNum
        );

        when(findUserPort.findAllStatusUserById(id)).thenReturn(Optional.of(user));

        // when
        GetUserInfoResult result = getUserService.getAllStatusUserInfo(id);

        // then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.fullName()).isEqualTo(fullName);
        assertThat(result.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(result.referenceCode()).isEqualTo(referenceCode);
        assertThat(result.roleId()).isEqualTo(role.getId());
        assertThat(result.signedUpAt()).isEqualTo(signedUpAt);
        assertThat(result.lastLoggedInAt()).isEqualTo(lastLoggedInAt);
        assertThat(result.status()).isEqualTo(status.name());
        assertThat(result.isWithdrawn()).isTrue();
        assertThat(result.orderNum()).isEqualTo(orderNum);
        assertThat(result.accountInfo().accounts()).containsExactly(
                new AccountResult(AuthVendor.KAKAO, "kakao-oidc-2")
        );

        verify(findUserPort).findAllStatusUserById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 전체 회원을 조회하면 예외를 던진다")
    void getAllStatusUserInfo_notFound_throws() {
        // given
        Long id = 100L;
        when(findUserPort.findAllStatusUserById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getAllStatusUserInfo(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findAllStatusUserById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID를 전송해 회원 도메인을 조회한다")
    void getUser_success_returnsUser() {
        // given
        Long id = 10L;
        User user = UserTestObjectFactory.createDefaultUser(id, EntityStatus.ACTIVE, false, List.of());

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getUser(id);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID를 전송해 전체 회원 도메인을 조회한다")
    void getAllStatusUser_byId_success_returnsUser() {
        // given
        Long id = 13L;
        User user = UserTestObjectFactory.createDefaultUser(id, EntityStatus.INACTIVE, true, List.of());

        when(findUserPort.findAllStatusUserById(id)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getAllStatusUser(id);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findAllStatusUserById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID로 전체 회원 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getAllStatusUser_byId_notFound_throws() {
        // given
        Long id = 101L;
        when(findUserPort.findAllStatusUserById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getAllStatusUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findAllStatusUserById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("이메일을 전송해 전체 회원 도메인을 조회한다")
    void getAllStatusUser_byEmail_success_returnsUser() {
        // given
        String email = "allstatus@test.com";
        User user = UserTestObjectFactory.createDefaultUser(14L, EntityStatus.UNEXPOSED, false, List.of());

        when(findUserPort.findAllStatusUserByEmail(email)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getAllStatusUser(email);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findAllStatusUserByEmail(email);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("이메일로 전체 회원 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getAllStatusUser_byEmail_notFound_throws() {
        // given
        String email = "missing@test.com";
        when(findUserPort.findAllStatusUserByEmail(email)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getAllStatusUser(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_EMAIL_NOT_FOUND_EXCEPTION_MESSAGE, email));

        verify(findUserPort).findAllStatusUserByEmail(email);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("Auth Vendor와 OIDC ID를 전송해 전체 회원 도메인을 조회한다")
    void getAllStatusUser_byAuthVendorAndOidcId_success_returnsUser() {
        // given
        AuthVendor authVendor = AuthVendor.APPLE;
        String oidcId = "oidc-apple";
        User user = UserTestObjectFactory.createDefaultUser(15L, EntityStatus.ACTIVE, false, List.of());

        when(findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getAllStatusUser(authVendor, oidcId);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("Auth Vendor와 OIDC ID로 전체 회원 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getAllStatusUser_byAuthVendorAndOidcId_notFound_throws() {
        // given
        AuthVendor authVendor = AuthVendor.KAKAO;
        String oidcId = "missing-oidc-all";
        when(findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getAllStatusUser(authVendor, oidcId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_OIDC_ID_NOT_FOUND_EXCEPTION_MESSAGE, oidcId));

        verify(findUserPort).findByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID로 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getUser_notFound_throws() {
        // given
        Long id = 404L;
        when(findUserPort.findById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getUser(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("인증 제공자와 OIDC ID를 전송해 회원 도메인을 조회한다")
    void getUser_byAuthVendorAndOidcId_success_returnsUser() {
        // given
        AuthVendor authVendor = AuthVendor.KAKAO;
        String oidcId = "oidc-123";
        User user = UserTestObjectFactory.createDefaultUser(11L, EntityStatus.ACTIVE, false, List.of());

        when(findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getUser(authVendor, oidcId);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("인증 제공자와 OIDC ID로 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getUser_byAuthVendorAndOidcId_notFound_throws() {
        // given
        AuthVendor authVendor = AuthVendor.GOOGLE;
        String oidcId = "missing-oidc";
        when(findUserPort.findByAuthVendorAndOidcId(authVendor, oidcId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getUser(authVendor, oidcId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_OIDC_ID_NOT_FOUND_EXCEPTION_MESSAGE, oidcId));

        verify(findUserPort).findByAuthVendorAndOidcId(authVendor, oidcId);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("추천 코드를 전송해 회원 도메인을 조회한다")
    void getUser_byReferenceCode_success_returnsUser() {
        // given
        String referenceCode = "ref-123";
        User user = UserTestObjectFactory.createDefaultUser(12L, EntityStatus.ACTIVE, false, List.of());

        when(findUserPort.findByReferenceCode(referenceCode)).thenReturn(Optional.of(user));

        // when
        User result = getUserService.getUser(referenceCode);

        // then
        assertThat(result).isSameAs(user);
        verify(findUserPort).findByReferenceCode(referenceCode);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("추천 코드로 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getUser_byReferenceCode_notFound_throws() {
        // given
        String referenceCode = "missing-ref";
        when(findUserPort.findByReferenceCode(referenceCode)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getUser(referenceCode))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_REFERENCE_CODE_NOT_FOUND_EXCEPTION_MESSAGE, referenceCode));

        verify(findUserPort).findByReferenceCode(referenceCode);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID를 전송해 회원 키를 조회한다")
    void getUserKey_success_returnsUserKey() {
        // given
        Long id = 20L;
        UUID userKey = UUID.fromString("00000000-0000-0000-0000-000000000020");

        when(findUserPort.findUserKeyById(id)).thenReturn(Optional.of(userKey));

        // when
        UUID result = getUserService.getUserKey(id);

        // then
        assertThat(result).isEqualTo(userKey);
        verify(findUserPort).findUserKeyById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 ID로 회원 키 조회 시 회원이 존재하지 않으면 예외를 던진다")
    void getUserKey_notFound_throws() {
        // given
        Long id = 21L;
        when(findUserPort.findUserKeyById(id)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getUserService.getUserKey(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id));

        verify(findUserPort).findUserKeyById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("전송한 추천인 코드를 가진 회원이 존재하면 true를 반환한다")
    void existsUser_whenReferenceCodeExists_returnsTrue() {
        // given
        String referenceCode = "ref-exists";
        when(findUserPort.existsByReferenceCode(referenceCode)).thenReturn(true);

        // when
        boolean result = getUserService.existsUser(referenceCode);

        // then
        assertThat(result).isTrue();
        verify(findUserPort).existsByReferenceCode(referenceCode);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("전송한 추천인 코드를 가진 회원이 존재하지 않으면 false를 반환한다")
    void existsUser_whenReferenceCodeMissing_returnsFalse() {
        // given
        String referenceCode = "ref-missing";
        when(findUserPort.existsByReferenceCode(referenceCode)).thenReturn(false);

        // when
        boolean result = getUserService.existsUser(referenceCode);

        // then
        assertThat(result).isFalse();
        verify(findUserPort).existsByReferenceCode(referenceCode);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 목록 조회 시 페이징과 검색 조건을 전달하고 결과를 매핑한다")
    void getAllStatusUsers_success_mapsResultsAndUsesPageable() {
        // given
        int pageSize = 2;
        int pageNumber = 1;
        Sort.Direction sortDirection = Sort.Direction.DESC;
        UserSortProperty sortProperty = UserSortProperty.PHONE_NUMBER;
        UserSearchTarget searchTarget = UserSearchTarget.EMAIL;
        String searchKeyword = "test";

        User user1 = UserTestObjectFactory.createUser(
                30L,
                "nick1",
                "user1@test.com",
                "홍길동",
                "010-1000-0001",
                "ref-1",
                Role.getBuyer(),
                List.of(UserAuthProvider.of(AuthVendor.KAKAO, "kakao-1")),
                LocalDateTime.of(2024, 3, 1, 9, 0),
                LocalDateTime.of(2024, 3, 2, 10, 0),
                EntityStatus.ACTIVE,
                false,
                1L
        );
        User user2 = UserTestObjectFactory.createUser(
                31L,
                "nick2",
                "user2@test.com",
                "김철수",
                "010-1000-0002",
                "ref-2",
                Role.getBuyer(),
                List.of(UserAuthProvider.of(AuthVendor.GOOGLE, "google-2")),
                LocalDateTime.of(2024, 3, 3, 9, 0),
                LocalDateTime.of(2024, 3, 4, 10, 0),
                EntityStatus.INACTIVE,
                true,
                2L
        );

        Page<User> users = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getCamelCaseValue())),
                4
        );

        when(findUserPort.findAllStatusUsersByPage(
                ArgumentMatchers.any(Pageable.class),
                ArgumentMatchers.eq(searchTarget),
                ArgumentMatchers.eq(searchKeyword)
        )).thenReturn(users);

        // when
        Page<GetUserResult> result = getUserService.getAllStatusUsers(
                pageSize,
                pageNumber,
                sortDirection,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getContent()).hasSize(2);

        GetUserResult first = result.getContent().get(0);
        assertThat(first.id()).isEqualTo(30L);
        assertThat(first.nickname()).isEqualTo("nick1");
        assertThat(first.email()).isEqualTo("user1@test.com");
        assertThat(first.fullName()).isEqualTo("홍길동");
        assertThat(first.phoneNumber()).isEqualTo("010-1000-0001");
        assertThat(first.referenceCode()).isEqualTo("ref-1");
        assertThat(first.roleId()).isEqualTo(Role.getBuyer().getId());
        assertThat(first.signedUpAt()).isEqualTo(LocalDateTime.of(2024, 3, 1, 9, 0));
        assertThat(first.lastLoggedInAt()).isEqualTo(LocalDateTime.of(2024, 3, 2, 10, 0));
        assertThat(first.status()).isEqualTo(EntityStatus.ACTIVE.name());
        assertThat(first.isWithdrawn()).isFalse();
        assertThat(first.orderNum()).isEqualTo(1L);
        assertThat(first.accountInfo().accounts()).containsExactly(
                new AccountResult(AuthVendor.KAKAO, "kakao-1")
        );

        GetUserResult second = result.getContent().get(1);
        assertThat(second.id()).isEqualTo(31L);
        assertThat(second.nickname()).isEqualTo("nick2");
        assertThat(second.email()).isEqualTo("user2@test.com");
        assertThat(second.fullName()).isEqualTo("김철수");
        assertThat(second.phoneNumber()).isEqualTo("010-1000-0002");
        assertThat(second.referenceCode()).isEqualTo("ref-2");
        assertThat(second.roleId()).isEqualTo(Role.getBuyer().getId());
        assertThat(second.signedUpAt()).isEqualTo(LocalDateTime.of(2024, 3, 3, 9, 0));
        assertThat(second.lastLoggedInAt()).isEqualTo(LocalDateTime.of(2024, 3, 4, 10, 0));
        assertThat(second.status()).isEqualTo(EntityStatus.INACTIVE.name());
        assertThat(second.isWithdrawn()).isTrue();
        assertThat(second.orderNum()).isEqualTo(2L);
        assertThat(second.accountInfo().accounts()).containsExactly(
                new AccountResult(AuthVendor.GOOGLE, "google-2")
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(findUserPort).findAllStatusUsersByPage(
                pageableCaptor.capture(),
                ArgumentMatchers.eq(searchTarget),
                ArgumentMatchers.eq(searchKeyword)
        );

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);
        Sort.Order order = pageable.getSort().getOrderFor(sortProperty.getCamelCaseValue());
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(sortDirection);

        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("회원 목록이 비어 있으면 빈 페이지를 반환한다")
    void getAllStatusUsers_empty_returnsEmptyPage() {
        // given
        int pageSize = 10;
        int pageNumber = 0;
        Sort.Direction sortDirection = Sort.Direction.ASC;
        UserSortProperty sortProperty = UserSortProperty.ID;
        UserSearchTarget searchTarget = UserSearchTarget.NICKNAME;
        String searchKeyword = "none";

        Page<User> users = new PageImpl<>(
                List.of(),
                PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortProperty.getCamelCaseValue())),
                0
        );

        when(findUserPort.findAllStatusUsersByPage(
                ArgumentMatchers.any(Pageable.class),
                ArgumentMatchers.eq(searchTarget),
                ArgumentMatchers.eq(searchKeyword)
        )).thenReturn(users);

        // when
        Page<GetUserResult> result = getUserService.getAllStatusUsers(
                pageSize,
                pageNumber,
                sortDirection,
                sortProperty,
                searchTarget,
                searchKeyword
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(findUserPort).findAllStatusUsersByPage(
                ArgumentMatchers.any(Pageable.class),
                ArgumentMatchers.eq(searchTarget),
                ArgumentMatchers.eq(searchKeyword)
        );
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("비활성 회원이면 상태와 탈퇴 여부를 반환한다")
    void getUserInfo_inactiveUser_mapsStatusAndWithdrawn() {
        // given
        Long id = 2L;
        User user = UserTestObjectFactory.createDefaultUser(id, EntityStatus.INACTIVE, true, List.of());

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));

        // when
        GetUserInfoResult result = getUserService.getUserInfo(id);

        // then
        assertThat(result.status()).isEqualTo(EntityStatus.INACTIVE.name());
        assertThat(result.isWithdrawn()).isTrue();
        assertThat(result.accountInfo().accounts()).isEmpty();

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

    @Test
    @DisplayName("비노출 회원이면 상태와 계정 목록을 반환한다")
    void getUserInfo_unexposedUser_mapsStatusAndAccounts() {
        // given
        Long id = 3L;
        List<UserAuthProvider> userAuthProviders = List.of(
                UserAuthProvider.of(AuthVendor.APPLE, "apple-oidc")
        );
        User user = UserTestObjectFactory.createDefaultUser(id, EntityStatus.UNEXPOSED, false, userAuthProviders);

        when(findUserPort.findById(id)).thenReturn(Optional.of(user));

        // when
        GetUserInfoResult result = getUserService.getUserInfo(id);

        // then
        assertThat(result.status()).isEqualTo(EntityStatus.UNEXPOSED.name());
        assertThat(result.isWithdrawn()).isFalse();
        assertThat(result.accountInfo().accounts()).containsExactly(
                new AccountResult(AuthVendor.APPLE, "apple-oidc")
        );

        verify(findUserPort).findById(id);
        verifyNoMoreInteractions(findUserPort);
    }

}
