package com.personal.marketnote.user.adapter.out.persistence;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.exception.UserNotFoundException;
import com.personal.marketnote.user.adapter.out.mapper.UserJpaEntityToDomainMapper;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.LoginHistoryJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserPenaltyHistoryJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.entity.UserStatusHistoryJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.LoginHistoryJpaRepository;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.TermsJpaRepository;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.UserJpaRepository;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.UserPenaltyHistoryJpaRepository;
import com.personal.marketnote.user.adapter.out.persistence.user.repository.UserStatusHistoryJpaRepository;
import com.personal.marketnote.user.domain.user.*;
import com.personal.marketnote.user.port.out.user.FindLoginHistoryPort;
import com.personal.marketnote.user.port.out.user.FindTermsPort;
import com.personal.marketnote.user.port.out.user.FindUserPenaltyHistoryPort;
import com.personal.marketnote.user.port.out.user.FindUserPort;
import com.personal.marketnote.user.port.out.user.FindUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.SaveLoginHistoryPort;
import com.personal.marketnote.user.port.out.user.SaveUserPenaltyHistoryPort;
import com.personal.marketnote.user.port.out.user.SaveUserPort;
import com.personal.marketnote.user.port.out.user.SaveUserStatusHistoryPort;
import com.personal.marketnote.user.port.out.user.UpdateUserPort;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.personal.marketnote.user.exception.ExceptionMessage.USER_ID_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPersistenceAdapter
        implements SaveUserPort, FindUserPort, FindTermsPort, UpdateUserPort, SaveLoginHistoryPort, FindLoginHistoryPort,
        SaveUserPenaltyHistoryPort, FindUserPenaltyHistoryPort,
        SaveUserStatusHistoryPort, FindUserStatusHistoryPort {
    private final UserJpaRepository userJpaRepository;
    private final TermsJpaRepository termsJpaRepository;
    private final LoginHistoryJpaRepository loginHistoryJpaRepository;
    private final UserPenaltyHistoryJpaRepository userPenaltyHistoryJpaRepository;
    private final UserStatusHistoryJpaRepository userStatusHistoryJpaRepository;

    @Override
    public User save(User user) {
        UserJpaEntity savedEntity = userJpaRepository.save(UserJpaEntity.from(user, termsJpaRepository));
        savedEntity.setIdToOrderNum();

        return UserJpaEntityToDomainMapper.mapToDomain(savedEntity).orElse(null);
    }

    @Override
    public boolean existsByAuthVendorAndOidcId(AuthVendor authVendor, String oidcId) {
        return userJpaRepository.existsByAuthVendorAndOidcId(authVendor, oidcId);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userJpaRepository.existsByNickname(nickname);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userJpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByReferenceCode(String referenceCode) {
        return userJpaRepository.existsByReferenceCode(referenceCode);
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 120)
    public Optional<User> findById(Long id) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findById(id).orElse(null));
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 120)
    public Optional<User> findByAuthVendorAndOidcId(AuthVendor authVendor, String oidcId) {
        return UserJpaEntityToDomainMapper.mapToDomain(
                userJpaRepository.findByAuthVendorAndOidcId(authVendor, oidcId).orElse(null)
        );
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findByPhoneNumber(phoneNumber).orElse(null));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findByEmail(email).orElse(null));
    }

    @Override
    public Optional<User> findByReferenceCode(String referredUserCode) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findByReferenceCode(referredUserCode).orElse(null));
    }

    @Override
    public Optional<User> findAllStatusUserById(Long id) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findAllStatusUserById(id).orElse(null));
    }

    @Override
    public Optional<User> findAllStatusUserByEmail(String email) {
        return UserJpaEntityToDomainMapper.mapToDomain(userJpaRepository.findAllStatusUserByEmail(email).orElse(null));
    }

    @Override
    public Optional<UUID> findUserKeyById(Long id) {
        return Optional.ofNullable(findEntityById(id).getUserKey());
    }

    @Override
    public List<User> findAllStatusUsers() {
        return userJpaRepository.findAllStatusUsers().stream()
                .map(UserJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Page<User> findAllStatusUsersByPage(Pageable pageable, UserSearchTarget searchTarget, String searchKeyword) {
        boolean byId = searchTarget == UserSearchTarget.ID;
        boolean byNickname = searchTarget == UserSearchTarget.NICKNAME;
        boolean byEmail = searchTarget == UserSearchTarget.EMAIL;
        boolean byPhone = searchTarget == UserSearchTarget.PHONE_NUMBER;
        boolean byRefCode = searchTarget == UserSearchTarget.REFERENCE_CODE;

        Page<UserJpaEntity> userJpaEntityPage = userJpaRepository.findAllStatusUsersByPage(
                pageable, byId, byNickname, byEmail, byPhone, byRefCode, searchKeyword
        );

        List<User> users = userJpaEntityPage
                .stream()
                .map(UserJpaEntityToDomainMapper::mapToDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, userJpaEntityPage.getTotalElements());
    }

    @Override
    public List<Terms> findAll() {
        return termsJpaRepository.findAllByOrderByIdAsc().stream()
                .map(UserJpaEntityToDomainMapper::mapToTermsDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public void update(User user) throws UserNotFoundException {
        UserJpaEntity userJpaEntity = findEntityById(user.getId());
        userJpaEntity.updateFrom(user);
    }

    @Override
    public void saveLoginHistory(LoginHistory loginHistory) {
        Long userId = loginHistory.getUser().getId();
        UserJpaEntity userRef = userJpaRepository.getReferenceById(userId);
        userRef.updateLoginTime();
        LoginHistoryJpaEntity entity = LoginHistoryJpaEntity.of(
                userRef, loginHistory.getAuthVendor(), loginHistory.getIpAddress()
        );
        loginHistoryJpaRepository.save(entity);
    }

    @Override
    public Page<LoginHistory> findLoginHistoriesByUserId(Pageable pageable, Long userId) {
        Page<LoginHistoryJpaEntity> page = loginHistoryJpaRepository.findLoginHistoriesByUserId(pageable, userId);

        List<LoginHistory> histories = page.stream()
                .map(e -> LoginHistory.from(
                        LoginHistorySnapshotState.builder()
                                .id(e.getId())
                                .user(UserJpaEntityToDomainMapper.mapToDomain(e.getUserJpaEntity()).orElseGet(
                                        () -> com.personal.marketnote.user.domain.user.User.referenceOf(e.getUserJpaEntity().getId())
                                ))
                                .authVendor(e.getAuthVendor())
                                .ipAddress(e.getIpAddress())
                                .createdAt(e.getCreatedAt())
                                .build()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(histories, pageable, page.getTotalElements());
    }

    private UserJpaEntity findEntityById(Long id) throws UserNotFoundException {
        return userJpaRepository.findAllStatusUserById(id).orElseThrow(
                () -> new UserNotFoundException(String.format(USER_ID_NOT_FOUND_EXCEPTION_MESSAGE, id))
        );
    }

    @Override
    public UserStatusHistory save(UserStatusHistory history) {
        UserStatusHistoryJpaEntity saved = userStatusHistoryJpaRepository.save(
                UserStatusHistoryJpaEntity.from(history)
        );
        return saved.toDomain();
    }

    @Override
    public UserPenaltyHistory save(UserPenaltyHistory history) {
        UserPenaltyHistoryJpaEntity saved = userPenaltyHistoryJpaRepository.save(
                UserPenaltyHistoryJpaEntity.from(history)
        );
        return saved.toDomain();
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 120)
    public Page<UserPenaltyHistory> findUserPenaltyHistoriesByUserId(Pageable pageable, Long userId) {
        Page<UserPenaltyHistoryJpaEntity> page = userPenaltyHistoryJpaRepository.findUserPenaltyHistoriesByUserId(pageable, userId);
        List<UserPenaltyHistory> histories = page.stream()
                .map(UserPenaltyHistoryJpaEntity::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(histories, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true, timeout = 120)
    public Page<UserStatusHistory> findUserStatusHistoriesByUserId(Pageable pageable, Long userId) {
        Page<UserStatusHistoryJpaEntity> page = userStatusHistoryJpaRepository.findUserStatusHistoriesByUserId(pageable, userId);
        List<UserStatusHistory> histories = page.stream()
                .map(UserStatusHistoryJpaEntity::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(histories, pageable, page.getTotalElements());
    }
}
