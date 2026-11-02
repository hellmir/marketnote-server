package com.personal.marketnote.user.adapter.out.persistence.remotearea;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.exception.RemoteAreaAlreadyExistsException;
import com.personal.marketnote.user.exception.RemoteAreaNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteAreaPersistenceAdapterTest {
    @InjectMocks
    private RemoteAreaPersistenceAdapter remoteAreaPersistenceAdapter;

    @Mock
    private RemoteAreaJpaRepository remoteAreaJpaRepository;

    private RemoteArea createRemoteArea(Long id) {
        return RemoteArea.from(RemoteAreaSnapshotState.builder()
                .id(id)
                .province("제주특별자치도")
                .district("제주시")
                .village("추자면")
                .subarea("")
                .regionType(ShippingAddressRegionType.ISLAND)
                .build());
    }

    private RemoteAreaJpaEntity createEntity(Long id) {
        RemoteAreaJpaEntity entity = mock(RemoteAreaJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getProvince()).thenReturn("제주특별자치도");
        when(entity.getDistrict()).thenReturn("제주시");
        when(entity.getVillage()).thenReturn("추자면");
        when(entity.getSubarea()).thenReturn("");
        when(entity.getRegionType()).thenReturn(ShippingAddressRegionType.ISLAND);
        return entity;
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("도서산간 지역을 저장한다")
        void savesRemoteArea() {
            // given
            RemoteArea remoteArea = createRemoteArea(null);

            // when
            remoteAreaPersistenceAdapter.save(remoteArea);

            // then
            verify(remoteAreaJpaRepository).save(any(RemoteAreaJpaEntity.class));
        }

        @Test
        @DisplayName("중복된 주소로 저장 시 RemoteAreaAlreadyExistsException이 발생한다")
        void throwsExceptionWhenDuplicate() {
            // given
            RemoteArea remoteArea = createRemoteArea(null);
            when(remoteAreaJpaRepository.save(any(RemoteAreaJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("unique constraint"));

            // when & then
            assertThatThrownBy(() -> remoteAreaPersistenceAdapter.save(remoteArea))
                    .isInstanceOf(RemoteAreaAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("existsByAddress")
    class ExistsByAddress {
        @Test
        @DisplayName("주소가 존재하면 true를 반환한다")
        void returnsTrueWhenExists() {
            // given
            when(remoteAreaJpaRepository.existsByProvinceAndDistrictAndVillageAndSubareaAndStatus(
                    "제주특별자치도", "제주시", "추자면", "", EntityStatus.ACTIVE
            )).thenReturn(true);

            // when
            boolean result = remoteAreaPersistenceAdapter.existsByAddress("제주특별자치도", "제주시", "추자면", "");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("주소가 존재하지 않으면 false를 반환한다")
        void returnsFalseWhenNotExists() {
            // given
            when(remoteAreaJpaRepository.existsByProvinceAndDistrictAndVillageAndSubareaAndStatus(
                    "서울특별시", "", "", "", EntityStatus.ACTIVE
            )).thenReturn(false);

            // when
            boolean result = remoteAreaPersistenceAdapter.existsByAddress("서울특별시", "", "", "");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllActive")
    class FindAllActive {
        @Test
        @DisplayName("활성 상태의 도서산간 지역 목록을 반환한다")
        void returnsActiveRemoteAreas() {
            // given
            RemoteAreaJpaEntity entity = createEntity(1L);
            when(remoteAreaJpaRepository.findAllByStatus(EntityStatus.ACTIVE)).thenReturn(List.of(entity));

            // when
            List<RemoteArea> result = remoteAreaPersistenceAdapter.findAllActive();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getProvince()).isEqualTo("제주특별자치도");
        }

        @Test
        @DisplayName("활성 도서산간 지역이 없으면 빈 리스트를 반환한다")
        void returnsEmptyListWhenNone() {
            // given
            when(remoteAreaJpaRepository.findAllByStatus(EntityStatus.ACTIVE)).thenReturn(List.of());

            // when
            List<RemoteArea> result = remoteAreaPersistenceAdapter.findAllActive();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {
        @Test
        @DisplayName("활성 도서산간 지역이 존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenExists() {
            // given
            RemoteAreaJpaEntity entity = createEntity(1L);
            when(remoteAreaJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<RemoteArea> result = remoteAreaPersistenceAdapter.findActiveById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않으면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(remoteAreaJpaRepository.findByIdAndStatus(999L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<RemoteArea> result = remoteAreaPersistenceAdapter.findActiveById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {
        @Test
        @DisplayName("도서산간 지역이 존재하면 비활성화한다")
        void deactivatesWhenExists() {
            // given
            RemoteArea remoteArea = createRemoteArea(1L);
            RemoteAreaJpaEntity entity = mock(RemoteAreaJpaEntity.class);
            when(remoteAreaJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            remoteAreaPersistenceAdapter.deactivate(remoteArea);

            // then
            verify(entity).markInactive();
        }

        @Test
        @DisplayName("도서산간 지역이 존재하지 않으면 RemoteAreaNotFoundException이 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            RemoteArea remoteArea = createRemoteArea(999L);
            when(remoteAreaJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> remoteAreaPersistenceAdapter.deactivate(remoteArea))
                    .isInstanceOf(RemoteAreaNotFoundException.class);
        }
    }
}
