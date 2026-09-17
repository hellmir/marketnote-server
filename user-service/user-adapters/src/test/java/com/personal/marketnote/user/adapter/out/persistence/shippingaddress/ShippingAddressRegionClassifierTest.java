package com.personal.marketnote.user.adapter.out.persistence.shippingaddress;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.adapter.out.persistence.remotearea.repository.RemoteAreaJpaRepository;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingAddressRegionClassifierTest {

    @InjectMocks
    private ShippingAddressRegionClassifier shippingAddressRegionClassifier;

    @Mock
    private RemoteAreaJpaRepository remoteAreaJpaRepository;

    @Test
    @DisplayName("제주특별자치도 주소는 JEJU로 분류된다")
    void classify_jejuSpecialAutonomousProvince_returnsJeju() {
        // given
        String address = "제주특별자치도 제주시 한라산로 456";

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.JEJU);
        verifyNoInteractions(remoteAreaJpaRepository);
    }

    @Test
    @DisplayName("제주시로 시작하는 주소는 JEJU로 분류된다")
    void classify_jejuCity_returnsJeju() {
        // given
        String address = "제주시 한라산로 456";

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.JEJU);
        verifyNoInteractions(remoteAreaJpaRepository);
    }

    @Test
    @DisplayName("도서산간 지역 주소는 ISLAND로 분류된다")
    void classify_remoteAreaAddress_returnsIsland() {
        // given
        String address = "인천광역시 옹진군 영흥면 선재리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("인천", "옹진군", "", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("인천", "옹진군", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("인천", "옹진군", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("일반 지역 주소는 NORMAL로 분류된다")
    void classify_normalAddress_returnsNormal() {
        // given
        String address = "서울특별시 강남구 테헤란로 123";
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("서울", "강남구", EntityStatus.ACTIVE))
                .thenReturn(List.of());

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("서울", "강남구", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("경기도 주소에서 도서산간이 아닌 경우 NORMAL로 분류된다")
    void classify_gyeonggiNonRemote_returnsNormal() {
        // given
        String address = "경기도 성남시 분당구 판교로 123";
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("경기", "성남시", EntityStatus.ACTIVE))
                .thenReturn(List.of());

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("경기", "성남시", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("경상남도 도서산간 지역은 ISLAND로 분류된다")
    void classify_gyeongsangRemoteArea_returnsIsland() {
        // given
        String address = "경상남도 통영시 한산면 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("경남", "통영시", "", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("경남", "통영시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("경남", "통영시", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("세종특별자치시 주소는 NORMAL로 분류된다")
    void classify_sejongAddress_returnsNormal() {
        // given
        String address = "세종특별자치시 한누리대로 123";
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("세종", "한누리대로", EntityStatus.ACTIVE))
                .thenReturn(List.of());

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
    }

    @Test
    @DisplayName("공백 구분 토큰이 1개뿐인 주소는 NORMAL로 분류된다")
    void classify_singleTokenAddress_returnsNormal() {
        // given
        String address = "서울";

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
        verifyNoInteractions(remoteAreaJpaRepository);
    }

    @Test
    @DisplayName("null 주소 입력 시 NORMAL로 분류된다")
    void classify_nullAddress_returnsNormal() {
        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(null);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
        verifyNoInteractions(remoteAreaJpaRepository);
    }

    @Test
    @DisplayName("빈 문자열 주소 입력 시 NORMAL로 분류된다")
    void classify_emptyAddress_returnsNormal() {
        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify("");

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
        verifyNoInteractions(remoteAreaJpaRepository);
    }

    @Test
    @DisplayName("충청남도 주소에서 도서산간 지역은 ISLAND로 분류된다")
    void classify_chungnamRemoteArea_returnsIsland() {
        // given
        String address = "충청남도 보령시 오천면 삽시도리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("충남", "보령시", "", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("전라남도 주소에서 도서산간 지역은 ISLAND로 분류된다")
    void classify_jeonnamRemoteArea_returnsIsland() {
        // given
        String address = "전라남도 신안군 지도읍 선도리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("전남", "신안군", "", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("전남", "신안군", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
        verify(remoteAreaJpaRepository).findAllByProvinceAndDistrictAndStatus("전남", "신안군", EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("읍면동까지 일치하는 도서산간 레코드가 있으면 ISLAND로 분류된다")
    void classify_villageMatchRemoteArea_returnsIsland() {
        // given
        String address = "경상남도 통영시 한산면 추봉리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("경남", "통영시", "한산면", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("경남", "통영시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
    }

    @Test
    @DisplayName("읍면동이 불일치하면 NORMAL로 분류된다")
    void classify_villageMismatch_returnsNormal() {
        // given
        String address = "경상남도 통영시 광도면 죽림리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("경남", "통영시", "한산면", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("경남", "통영시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
    }

    @Test
    @DisplayName("세부지역까지 일치하는 레코드가 있으면 해당 regionType으로 분류된다")
    void classify_subareaMatch_returnsMatchedRegionType() {
        // given
        String address = "충청남도 보령시 오천면 외연도리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("충남", "보령시", "오천면", "외연도리", ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
    }

    @Test
    @DisplayName("세부지역이 불일치하면 NORMAL로 분류된다")
    void classify_subareaMismatch_returnsNormal() {
        // given
        String address = "충청남도 보령시 오천면 삽시도리 123";
        RemoteAreaJpaEntity entity = createRemoteAreaEntity("충남", "보령시", "오천면", "외연도리", ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.NORMAL);
    }

    @Test
    @DisplayName("DELIVERY_IMPOSSIBLE 레코드가 있으면 ISLAND보다 우선 적용된다")
    void classify_deliveryImpossiblePriority_overIsland() {
        // given
        String address = "충청남도 보령시 오천면 외연도리 123";
        RemoteAreaJpaEntity islandEntity = createRemoteAreaEntity("충남", "보령시", "", "", ShippingAddressRegionType.ISLAND);
        RemoteAreaJpaEntity impossibleEntity = createRemoteAreaEntity("충남", "보령시", "오천면", "외연도리", ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE))
                .thenReturn(List.of(islandEntity, impossibleEntity));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
    }

    @Test
    @DisplayName("여러 ISLAND 레코드 중 일치하는 것이 있으면 ISLAND로 분류된다")
    void classify_multipleIslandRecords_returnsIsland() {
        // given
        String address = "경상남도 통영시 한산면 추봉리 123";
        RemoteAreaJpaEntity entity1 = createRemoteAreaEntity("경남", "통영시", "사량면", "", ShippingAddressRegionType.ISLAND);
        RemoteAreaJpaEntity entity2 = createRemoteAreaEntity("경남", "통영시", "한산면", "", ShippingAddressRegionType.ISLAND);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("경남", "통영시", EntityStatus.ACTIVE))
                .thenReturn(List.of(entity1, entity2));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.ISLAND);
    }

    @Test
    @DisplayName("district 레벨 매칭과 village 레벨 DELIVERY_IMPOSSIBLE이 공존하면 DELIVERY_IMPOSSIBLE이 우선한다")
    void classify_districtIslandAndVillageImpossible_returnsImpossible() {
        // given
        String address = "충청남도 보령시 오천면 삽시도리 123";
        RemoteAreaJpaEntity districtLevel = createRemoteAreaEntity("충남", "보령시", "", "", ShippingAddressRegionType.ISLAND);
        RemoteAreaJpaEntity villageLevel = createRemoteAreaEntity("충남", "보령시", "오천면", "", ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
        when(remoteAreaJpaRepository.findAllByProvinceAndDistrictAndStatus("충남", "보령시", EntityStatus.ACTIVE))
                .thenReturn(List.of(districtLevel, villageLevel));

        // when
        ShippingAddressRegionType result = shippingAddressRegionClassifier.classify(address);

        // then
        assertThat(result).isEqualTo(ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
    }

    private RemoteAreaJpaEntity createRemoteAreaEntity(String province, String district,
                                                       String village, String subarea,
                                                       ShippingAddressRegionType regionType) {
        RemoteArea remoteArea = RemoteArea.from(RemoteAreaSnapshotState.builder()
                .id(1L)
                .province(province)
                .district(district)
                .village(village)
                .subarea(subarea)
                .regionType(regionType)
                .build());
        return RemoteAreaJpaEntity.from(remoteArea);
    }
}
