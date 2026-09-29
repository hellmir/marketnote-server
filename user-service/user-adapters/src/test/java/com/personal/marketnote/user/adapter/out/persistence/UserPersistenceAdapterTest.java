package com.personal.marketnote.user.adapter.out.persistence;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.domain.ipaddress.IpAddress;
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
import com.personal.marketnote.user.domain.user.LoginHistory;
import com.personal.marketnote.user.domain.user.LoginHistorySnapshotState;
import com.personal.marketnote.user.domain.user.User;
import com.personal.marketnote.user.domain.user.UserPenaltyHistory;
import com.personal.marketnote.user.domain.user.UserPenaltyHistorySnapshotState;
import com.personal.marketnote.user.domain.user.UserSearchTarget;
import com.personal.marketnote.user.domain.user.UserStatusAction;
import com.personal.marketnote.user.domain.user.UserStatusActor;
import com.personal.marketnote.user.domain.user.UserStatusHistory;
import com.personal.marketnote.user.domain.user.UserStatusHistorySnapshotState;
import com.personal.marketnote.user.security.token.vendor.AuthVendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {
    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private TermsJpaRepository termsJpaRepository;

    @Mock
    private LoginHistoryJpaRepository loginHistoryJpaRepository;

    @Mock
    private UserPenaltyHistoryJpaRepository userPenaltyHistoryJpaRepository;

    @Mock
    private UserStatusHistoryJpaRepository userStatusHistoryJpaRepository;

    private MockedStatic<UserJpaEntityToDomainMapper> mapperMock;
    private MockedStatic<UserJpaEntity> userJpaEntityMock;

    @BeforeEach
    void setUp() {
        mapperMock = mockStatic(UserJpaEntityToDomainMapper.class);
        userJpaEntityMock = mockStatic(UserJpaEntity.class);
    }

    @AfterEach
    void tearDown() {
        mapperMock.close();
        userJpaEntityMock.close();
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("사용자를 저장하고 도메인 객체를 반환한다")
        void savesUserAndReturnsDomain() {
            // given
            User user = mock(User.class);
            UserJpaEntity builtEntity = mock(UserJpaEntity.class);
            UserJpaEntity savedEntity = mock(UserJpaEntity.class);
            User mappedUser = mock(User.class);

            userJpaEntityMock.when(() -> UserJpaEntity.from(user, termsJpaRepository)).thenReturn(builtEntity);
            when(userJpaRepository.save(builtEntity)).thenReturn(savedEntity);
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(savedEntity))
                    .thenReturn(Optional.of(mappedUser));

            // when
            User result = userPersistenceAdapter.save(user);

            // then
            assertThat(result).isEqualTo(mappedUser);
            verify(savedEntity).setIdToOrderNum();
        }

        @Test
        @DisplayName("매핑 결과가 비어있으면 null을 반환한다")
        void returnsNullWhenMappingIsEmpty() {
            // given
            User user = mock(User.class);
            UserJpaEntity builtEntity = mock(UserJpaEntity.class);
            UserJpaEntity savedEntity = mock(UserJpaEntity.class);

            userJpaEntityMock.when(() -> UserJpaEntity.from(user, termsJpaRepository)).thenReturn(builtEntity);
            when(userJpaRepository.save(builtEntity)).thenReturn(savedEntity);
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(savedEntity))
                    .thenReturn(Optional.empty());

            // when
            User result = userPersistenceAdapter.save(user);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("existsBy 메서드")
    class ExistsMethods {
        @Test
        @DisplayName("existsByAuthVendorAndOidcId는 리포지토리에 위임한다")
        void existsByAuthVendorAndOidcIdDelegates() {
            // given
            when(userJpaRepository.existsByAuthVendorAndOidcId(AuthVendor.KAKAO, "oidc-123")).thenReturn(true);

            // when
            boolean result = userPersistenceAdapter.existsByAuthVendorAndOidcId(AuthVendor.KAKAO, "oidc-123");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("existsByNickname은 리포지토리에 위임한다")
        void existsByNicknameDelegates() {
            // given
            when(userJpaRepository.existsByNickname("testNick")).thenReturn(false);

            // when
            boolean result = userPersistenceAdapter.existsByNickname("testNick");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("existsByEmail은 리포지토리에 위임한다")
        void existsByEmailDelegates() {
            // given
            when(userJpaRepository.existsByEmail("test@test.com")).thenReturn(true);

            // when
            boolean result = userPersistenceAdapter.existsByEmail("test@test.com");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("existsByPhoneNumber는 리포지토리에 위임한다")
        void existsByPhoneNumberDelegates() {
            // given
            when(userJpaRepository.existsByPhoneNumber("010-1234-5678")).thenReturn(true);

            // when
            boolean result = userPersistenceAdapter.existsByPhoneNumber("010-1234-5678");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("existsByReferenceCode는 리포지토리에 위임한다")
        void existsByReferenceCodeDelegates() {
            // given
            when(userJpaRepository.existsByReferenceCode("REF123")).thenReturn(false);

            // when
            boolean result = userPersistenceAdapter.existsByReferenceCode("REF123");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("사용자가 존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenUserExists() {
            // given
            UserJpaEntity entity = mock(UserJpaEntity.class);
            User user = mock(User.class);

            when(userJpaRepository.findById(1L)).thenReturn(Optional.of(entity));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.of(user));

            // when
            Optional<User> result = userPersistenceAdapter.findById(1L);

            // then
            assertThat(result).isPresent().contains(user);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenUserNotFound() {
            // given
            when(userJpaRepository.findById(999L)).thenReturn(Optional.empty());
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain((UserJpaEntity) null))
                    .thenReturn(Optional.empty());

            // when
            Optional<User> result = userPersistenceAdapter.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByAuthVendorAndOidcId")
    class FindByAuthVendorAndOidcId {
        @Test
        @DisplayName("인증 공급업체와 OIDC ID로 사용자를 조회한다")
        void returnsUserByAuthVendorAndOidcId() {
            // given
            UserJpaEntity entity = mock(UserJpaEntity.class);
            User user = mock(User.class);

            when(userJpaRepository.findByAuthVendorAndOidcId(AuthVendor.KAKAO, "oidc-123"))
                    .thenReturn(Optional.of(entity));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.of(user));

            // when
            Optional<User> result = userPersistenceAdapter.findByAuthVendorAndOidcId(AuthVendor.KAKAO, "oidc-123");

            // then
            assertThat(result).isPresent().contains(user);
        }
    }

    @Nested
    @DisplayName("findAllStatusUsersByPage")
    class FindAllStatusUsersByPage {
        @Test
        @DisplayName("ID 검색 대상으로 페이징 조회한다")
        void searchesByIdTarget() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            UserJpaEntity entity = mock(UserJpaEntity.class);
            User user = mock(User.class);
            Page<UserJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

            when(userJpaRepository.findAllStatusUsersByPage(
                    pageable, true, false, false, false, false, "1"
            )).thenReturn(entityPage);
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.of(user));

            // when
            Page<User> result = userPersistenceAdapter.findAllStatusUsersByPage(
                    pageable, UserSearchTarget.ID, "1"
            );

            // then
            assertThat(result.getContent()).hasSize(1).contains(user);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("매핑 실패한 엔티티는 결과에서 제외된다")
        void excludesUnmappableEntities() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            UserJpaEntity entity1 = mock(UserJpaEntity.class);
            UserJpaEntity entity2 = mock(UserJpaEntity.class);
            User user = mock(User.class);
            Page<UserJpaEntity> entityPage = new PageImpl<>(List.of(entity1, entity2), pageable, 2);

            when(userJpaRepository.findAllStatusUsersByPage(
                    pageable, false, true, false, false, false, "test"
            )).thenReturn(entityPage);
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity1))
                    .thenReturn(Optional.of(user));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity2))
                    .thenReturn(Optional.empty());

            // when
            Page<User> result = userPersistenceAdapter.findAllStatusUsersByPage(
                    pageable, UserSearchTarget.NICKNAME, "test"
            );

            // then
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllStatusUsers")
    class FindAllStatusUsers {
        @Test
        @DisplayName("모든 상태의 사용자 목록을 반환한다")
        void returnsAllStatusUsers() {
            // given
            UserJpaEntity entity = mock(UserJpaEntity.class);
            User user = mock(User.class);

            when(userJpaRepository.findAllStatusUsers()).thenReturn(List.of(entity));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.of(user));

            // when
            List<User> result = userPersistenceAdapter.findAllStatusUsers();

            // then
            assertThat(result).hasSize(1).contains(user);
        }

        @Test
        @DisplayName("매핑 실패한 엔티티는 결과에서 제외된다")
        void excludesUnmappableEntities() {
            // given
            UserJpaEntity entity = mock(UserJpaEntity.class);

            when(userJpaRepository.findAllStatusUsers()).thenReturn(List.of(entity));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.empty());

            // when
            List<User> result = userPersistenceAdapter.findAllStatusUsers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByDeactivatedUntilExpired")
    class FindAllByDeactivatedUntilExpired {
        @Test
        @DisplayName("비활성화 기간이 만료된 사용자 목록을 반환한다")
        void returnsExpiredDeactivatedUsers() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 14, 12, 0);
            UserJpaEntity entity = mock(UserJpaEntity.class);
            User user = mock(User.class);

            when(userJpaRepository.findAllByDeactivatedUntilExpired(now)).thenReturn(List.of(entity));
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(Optional.of(user));

            // when
            List<User> result = userPersistenceAdapter.findAllByDeactivatedUntilExpired(now);

            // then
            assertThat(result).hasSize(1).contains(user);
        }
    }

    @Nested
    @DisplayName("findUserKeyById")
    class FindUserKeyById {
        @Test
        @DisplayName("사용자가 존재하면 userKey를 반환한다")
        void returnsUserKeyWhenExists() {
            // given
            UUID userKey = UUID.randomUUID();
            UserJpaEntity entity = mock(UserJpaEntity.class);
            when(entity.getUserKey()).thenReturn(userKey);
            when(userJpaRepository.findAllStatusUserById(1L)).thenReturn(Optional.of(entity));

            // when
            Optional<UUID> result = userPersistenceAdapter.findUserKeyById(1L);

            // then
            assertThat(result).isPresent().contains(userKey);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 UserNotFoundException이 발생한다")
        void throwsExceptionWhenUserNotFound() {
            // given
            when(userJpaRepository.findAllStatusUserById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userPersistenceAdapter.findUserKeyById(999L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("사용자가 존재하면 엔티티를 업데이트한다")
        void updatesEntityWhenUserExists() {
            // given
            User user = mock(User.class);
            when(user.getId()).thenReturn(1L);
            UserJpaEntity entity = mock(UserJpaEntity.class);
            when(userJpaRepository.findAllStatusUserById(1L)).thenReturn(Optional.of(entity));

            // when
            userPersistenceAdapter.update(user);

            // then
            verify(entity).updateFrom(user);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 UserNotFoundException이 발생한다")
        void throwsExceptionWhenUserNotFound() {
            // given
            User user = mock(User.class);
            when(user.getId()).thenReturn(999L);
            when(userJpaRepository.findAllStatusUserById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userPersistenceAdapter.update(user))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("saveLoginHistory")
    class SaveLoginHistory {
        @Test
        @DisplayName("로그인 이력을 저장하고 사용자 로그인 시간을 업데이트한다")
        void savesLoginHistoryAndUpdatesLoginTime() {
            // given
            User user = mock(User.class);
            when(user.getId()).thenReturn(1L);

            LoginHistory loginHistory = LoginHistory.of(user, AuthVendor.KAKAO, "127.0.0.1");

            UserJpaEntity userRef = mock(UserJpaEntity.class);
            when(userJpaRepository.getReferenceById(1L)).thenReturn(userRef);

            // when
            userPersistenceAdapter.saveLoginHistory(loginHistory);

            // then
            verify(userRef).updateLoginTime();
            verify(loginHistoryJpaRepository).save(any(LoginHistoryJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("findLoginHistoriesByUserId")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class FindLoginHistoriesByUserId {
        @Test
        @DisplayName("사용자 ID로 로그인 이력을 페이징 조회한다")
        void returnsLoginHistoriesByPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            LoginHistoryJpaEntity loginEntity = mock(LoginHistoryJpaEntity.class);
            UserJpaEntity userEntity = mock(UserJpaEntity.class);
            when(userEntity.getId()).thenReturn(1L);
            when(loginEntity.getId()).thenReturn(10L);
            when(loginEntity.getUserJpaEntity()).thenReturn(userEntity);
            when(loginEntity.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
            when(loginEntity.getIpAddress()).thenReturn("127.0.0.1");
            when(loginEntity.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 4, 14, 12, 0));

            User mappedUser = mock(User.class);
            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(userEntity))
                    .thenReturn(Optional.of(mappedUser));

            Page<LoginHistoryJpaEntity> entityPage = new PageImpl<>(List.of(loginEntity), pageable, 1);
            when(loginHistoryJpaRepository.findLoginHistoriesByUserId(pageable, 1L)).thenReturn(entityPage);

            // when
            Page<LoginHistory> result = userPersistenceAdapter.findLoginHistoriesByUserId(pageable, 1L);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAuthVendor()).isEqualTo(AuthVendor.KAKAO);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("매핑 실패 시 referenceOf로 대체한다")
        void usesReferenceOfWhenMappingFails() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            LoginHistoryJpaEntity loginEntity = mock(LoginHistoryJpaEntity.class);
            UserJpaEntity userEntity = mock(UserJpaEntity.class);
            when(userEntity.getId()).thenReturn(1L);
            when(loginEntity.getId()).thenReturn(10L);
            when(loginEntity.getUserJpaEntity()).thenReturn(userEntity);
            when(loginEntity.getAuthVendor()).thenReturn(AuthVendor.KAKAO);
            when(loginEntity.getIpAddress()).thenReturn("127.0.0.1");
            when(loginEntity.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 4, 14, 12, 0));

            mapperMock.when(() -> UserJpaEntityToDomainMapper.mapToDomain(userEntity))
                    .thenReturn(Optional.empty());

            Page<LoginHistoryJpaEntity> entityPage = new PageImpl<>(List.of(loginEntity), pageable, 1);
            when(loginHistoryJpaRepository.findLoginHistoriesByUserId(pageable, 1L)).thenReturn(entityPage);

            // when
            Page<LoginHistory> result = userPersistenceAdapter.findLoginHistoriesByUserId(pageable, 1L);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("save(UserStatusHistory)")
    class SaveUserStatusHistory {
        @Test
        @DisplayName("사용자 상태 이력을 저장하고 도메인 객체를 반환한다")
        void savesAndReturnsDomain() {
            // given
            UserStatusHistory history = UserStatusHistory.byAdmin(
                    1L, UserStatusAction.DEACTIVATE, "테스트 사유",
                    LocalDateTime.of(2026, 5, 14, 12, 0), 100L
            );

            UserStatusHistoryJpaEntity savedEntity = mock(UserStatusHistoryJpaEntity.class);
            UserStatusHistory savedDomain = UserStatusHistory.from(
                    UserStatusHistorySnapshotState.builder()
                            .id(1L)
                            .userId(1L)
                            .statusAction(UserStatusAction.DEACTIVATE)
                            .reason("테스트 사유")
                            .deactivatedUntil(LocalDateTime.of(2026, 5, 14, 12, 0))
                            .actor(UserStatusActor.ADMIN)
                            .createdBy(100L)
                            .createdAt(LocalDateTime.of(2026, 4, 14, 12, 0))
                            .build()
            );

            when(userStatusHistoryJpaRepository.save(any(UserStatusHistoryJpaEntity.class))).thenReturn(savedEntity);
            when(savedEntity.toDomain()).thenReturn(savedDomain);

            // when
            UserStatusHistory result = userPersistenceAdapter.save(history);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getStatusAction()).isEqualTo(UserStatusAction.DEACTIVATE);
        }
    }

    @Nested
    @DisplayName("save(UserPenaltyHistory)")
    class SaveUserPenaltyHistory {
        @Test
        @DisplayName("사용자 제재 이력을 저장하고 도메인 객체를 반환한다")
        void savesAndReturnsDomain() {
            // given
            UserPenaltyHistory history = UserPenaltyHistory.of(1L, 0, 1, "테스트 사유", 100L);

            UserPenaltyHistoryJpaEntity savedEntity = mock(UserPenaltyHistoryJpaEntity.class);
            UserPenaltyHistory savedDomain = UserPenaltyHistory.from(
                    UserPenaltyHistorySnapshotState.builder()
                            .id(1L)
                            .userId(1L)
                            .previousCount(0)
                            .currentCount(1)
                            .reason("테스트 사유")
                            .createdBy(100L)
                            .createdAt(LocalDateTime.of(2026, 4, 14, 12, 0))
                            .build()
            );

            when(userPenaltyHistoryJpaRepository.save(any(UserPenaltyHistoryJpaEntity.class))).thenReturn(savedEntity);
            when(savedEntity.toDomain()).thenReturn(savedDomain);

            // when
            UserPenaltyHistory result = userPersistenceAdapter.save(history);

            // then
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPreviousCount()).isEqualTo(0);
            assertThat(result.getCurrentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findUserPenaltyHistoriesByUserId")
    class FindUserPenaltyHistoriesByUserId {
        @Test
        @DisplayName("사용자 ID로 제재 이력을 페이징 조회한다")
        void returnsPenaltyHistoriesByPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            UserPenaltyHistoryJpaEntity entity = mock(UserPenaltyHistoryJpaEntity.class);
            UserPenaltyHistory domain = UserPenaltyHistory.from(
                    UserPenaltyHistorySnapshotState.builder()
                            .id(1L).userId(1L).previousCount(0).currentCount(1)
                            .reason("사유").createdBy(100L)
                            .createdAt(LocalDateTime.of(2026, 4, 14, 12, 0))
                            .build()
            );

            when(entity.toDomain()).thenReturn(domain);
            Page<UserPenaltyHistoryJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            when(userPenaltyHistoryJpaRepository.findUserPenaltyHistoriesByUserId(pageable, 1L))
                    .thenReturn(entityPage);

            // when
            Page<UserPenaltyHistory> result = userPersistenceAdapter.findUserPenaltyHistoriesByUserId(pageable, 1L);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findUserStatusHistoriesByUserId")
    class FindUserStatusHistoriesByUserId {
        @Test
        @DisplayName("사용자 ID로 상태 변경 이력을 페이징 조회한다")
        void returnsStatusHistoriesByPage() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            UserStatusHistoryJpaEntity entity = mock(UserStatusHistoryJpaEntity.class);
            UserStatusHistory domain = UserStatusHistory.from(
                    UserStatusHistorySnapshotState.builder()
                            .id(1L).userId(1L)
                            .statusAction(UserStatusAction.DEACTIVATE)
                            .reason("사유")
                            .actor(UserStatusActor.ADMIN).createdBy(100L)
                            .createdAt(LocalDateTime.of(2026, 4, 14, 12, 0))
                            .build()
            );

            when(entity.toDomain()).thenReturn(domain);
            Page<UserStatusHistoryJpaEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);
            when(userStatusHistoryJpaRepository.findUserStatusHistoriesByUserId(pageable, 1L))
                    .thenReturn(entityPage);

            // when
            Page<UserStatusHistory> result = userPersistenceAdapter.findUserStatusHistoriesByUserId(pageable, 1L);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatusAction()).isEqualTo(UserStatusAction.DEACTIVATE);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }
}
