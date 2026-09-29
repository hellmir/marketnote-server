package com.personal.marketnote.user.adapter.out.persistence.shippingaddress;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.mapper.ShippingAddressJpaEntityToDomainMapper;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity.ShippingAddressJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.repository.ShippingAddressJpaRepository;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingAddressPersistenceAdapterTest {
    @InjectMocks
    private ShippingAddressPersistenceAdapter shippingAddressPersistenceAdapter;

    @Mock
    private ShippingAddressJpaRepository shippingAddressJpaRepository;

    private MockedStatic<ShippingAddressJpaEntity> entityMock;
    private MockedStatic<ShippingAddressJpaEntityToDomainMapper> mapperMock;

    @BeforeEach
    void setUp() {
        entityMock = mockStatic(ShippingAddressJpaEntity.class);
        mapperMock = mockStatic(ShippingAddressJpaEntityToDomainMapper.class);
    }

    @AfterEach
    void tearDown() {
        entityMock.close();
        mapperMock.close();
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("배송지를 저장하고 도메인 객체를 반환한다")
        void savesAndReturnsDomain() {
            // given
            ShippingAddress shippingAddress = mock(ShippingAddress.class);
            ShippingAddressJpaEntity builtEntity = mock(ShippingAddressJpaEntity.class);
            ShippingAddressJpaEntity savedEntity = mock(ShippingAddressJpaEntity.class);
            ShippingAddress mappedDomain = mock(ShippingAddress.class);

            entityMock.when(() -> ShippingAddressJpaEntity.from(shippingAddress)).thenReturn(builtEntity);
            when(shippingAddressJpaRepository.save(builtEntity)).thenReturn(savedEntity);
            mapperMock.when(() -> ShippingAddressJpaEntityToDomainMapper.mapToDomain(savedEntity))
                    .thenReturn(mappedDomain);

            // when
            ShippingAddress result = shippingAddressPersistenceAdapter.save(shippingAddress);

            // then
            assertThat(result).isEqualTo(mappedDomain);
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndAddressType")
    class ExistsByUserIdAndAddressType {
        @Test
        @DisplayName("사용자 ID와 주소 타입으로 존재 여부를 확인한다")
        void checksExistence() {
            // given
            when(shippingAddressJpaRepository.existsByUserIdAndAddressTypeAndStatus(
                    1L, ShippingAddressType.HOME, EntityStatus.ACTIVE
            )).thenReturn(true);

            // when
            boolean result = shippingAddressPersistenceAdapter.existsByUserIdAndAddressType(1L, ShippingAddressType.HOME);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("countByUserIdAndAddressType")
    class CountByUserIdAndAddressType {
        @Test
        @DisplayName("사용자 ID와 주소 타입으로 개수를 조회한다")
        void countsAddresses() {
            // given
            when(shippingAddressJpaRepository.countByUserIdAndAddressTypeAndStatus(
                    1L, ShippingAddressType.HOME, EntityStatus.ACTIVE
            )).thenReturn(3L);

            // when
            long result = shippingAddressPersistenceAdapter.countByUserIdAndAddressType(1L, ShippingAddressType.HOME);

            // then
            assertThat(result).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("existsByUserId")
    class ExistsByUserId {
        @Test
        @DisplayName("사용자 ID로 배송지 존재 여부를 확인한다")
        void checksExistenceByUserId() {
            // given
            when(shippingAddressJpaRepository.existsByUserIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(true);

            // when
            boolean result = shippingAddressPersistenceAdapter.existsByUserId(1L);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {
        @Test
        @DisplayName("사용자 ID로 배송지 목록을 조회한다")
        void returnsAddressList() {
            // given
            ShippingAddressJpaEntity entity = mock(ShippingAddressJpaEntity.class);
            ShippingAddress domain = mock(ShippingAddress.class);

            when(shippingAddressJpaRepository.findAllByUserIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(List.of(entity));
            mapperMock.when(() -> ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(domain);

            // when
            List<ShippingAddress> result = shippingAddressPersistenceAdapter.findAllByUserId(1L);

            // then
            assertThat(result).hasSize(1).contains(domain);
        }

        @Test
        @DisplayName("배송지가 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNone() {
            // given
            when(shippingAddressJpaRepository.findAllByUserIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(List.of());

            // when
            List<ShippingAddress> result = shippingAddressPersistenceAdapter.findAllByUserId(1L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {
        @Test
        @DisplayName("ID와 사용자 ID로 배송지를 조회한다")
        void returnsAddress() {
            // given
            ShippingAddressJpaEntity entity = mock(ShippingAddressJpaEntity.class);
            ShippingAddress domain = mock(ShippingAddress.class);

            when(shippingAddressJpaRepository.findByIdAndUserIdAndStatus(1L, 1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.of(entity));
            mapperMock.when(() -> ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(domain);

            // when
            Optional<ShippingAddress> result = shippingAddressPersistenceAdapter.findByIdAndUserId(1L, 1L);

            // then
            assertThat(result).isPresent().contains(domain);
        }

        @Test
        @DisplayName("존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(shippingAddressJpaRepository.findByIdAndUserIdAndStatus(999L, 1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<ShippingAddress> result = shippingAddressPersistenceAdapter.findByIdAndUserId(999L, 1L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDefaultsByUserId")
    class FindDefaultsByUserId {
        @Test
        @DisplayName("기본 배송지 목록을 조회한다")
        void returnsDefaultAddresses() {
            // given
            ShippingAddressJpaEntity entity = mock(ShippingAddressJpaEntity.class);
            ShippingAddress domain = mock(ShippingAddress.class);

            when(shippingAddressJpaRepository.findAllByUserIdAndIsDefaultAndStatus(1L, true, EntityStatus.ACTIVE))
                    .thenReturn(List.of(entity));
            mapperMock.when(() -> ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity))
                    .thenReturn(domain);

            // when
            List<ShippingAddress> result = shippingAddressPersistenceAdapter.findDefaultsByUserId(1L);

            // then
            assertThat(result).hasSize(1).contains(domain);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("배송지가 존재하면 엔티티를 업데이트한다")
        void updatesEntityWhenExists() {
            // given
            ShippingAddress shippingAddress = mock(ShippingAddress.class);
            when(shippingAddress.getId()).thenReturn(1L);
            ShippingAddressJpaEntity entity = mock(ShippingAddressJpaEntity.class);
            when(shippingAddressJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            shippingAddressPersistenceAdapter.update(shippingAddress);

            // then
            verify(entity).updateFrom(shippingAddress);
        }

        @Test
        @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            ShippingAddress shippingAddress = mock(ShippingAddress.class);
            when(shippingAddress.getId()).thenReturn(999L);
            when(shippingAddressJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shippingAddressPersistenceAdapter.update(shippingAddress))
                    .isInstanceOf(ShippingAddressNotFoundException.class);
        }
    }
}
