package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.user.adapter.out.persistence.remotearea.entity.RemoteAreaJpaEntity;
import com.personal.marketnote.user.domain.remotearea.RemoteArea;
import com.personal.marketnote.user.domain.remotearea.RemoteAreaCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RemoteAreaJpaEntityToDomainMapperTest {

    @Test
    @DisplayName("JPA 엔티티의 모든 필드를 도메인 객체로 매핑한다")
    void shouldMapAllFieldsToDomain() {
        // given
        RemoteArea source = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province("제주특별자치도")
                        .district("서귀포시")
                        .village("성산읍")
                        .subarea("오조리")
                        .regionType(ShippingAddressRegionType.JEJU)
                        .build()
        );
        RemoteAreaJpaEntity entity = RemoteAreaJpaEntity.from(source);
        ReflectionTestUtils.setField(entity, "id", 7L);

        // when
        RemoteArea domain = RemoteAreaJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(7L);
        assertThat(domain.getProvince()).isEqualTo("제주특별자치도");
        assertThat(domain.getDistrict()).isEqualTo("서귀포시");
        assertThat(domain.getVillage()).isEqualTo("성산읍");
        assertThat(domain.getSubarea()).isEqualTo("오조리");
        assertThat(domain.getRegionType()).isEqualTo(ShippingAddressRegionType.JEJU);
    }

    @Test
    @DisplayName("regionType이 ISLAND이면 도메인 객체에도 ISLAND로 매핑된다")
    void shouldMapIslandRegionType() {
        // given
        RemoteArea source = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province("인천광역시")
                        .district("옹진군")
                        .village("백령면")
                        .subarea("진촌리")
                        .regionType(ShippingAddressRegionType.ISLAND)
                        .build()
        );
        RemoteAreaJpaEntity entity = RemoteAreaJpaEntity.from(source);

        // when
        RemoteArea domain = RemoteAreaJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getRegionType()).isEqualTo(ShippingAddressRegionType.ISLAND);
        assertThat(domain.getProvince()).isEqualTo("인천광역시");
    }

    @Test
    @DisplayName("regionType이 DELIVERY_IMPOSSIBLE이면 도메인 객체에도 DELIVERY_IMPOSSIBLE로 매핑된다")
    void shouldMapDeliveryImpossibleRegionType() {
        // given
        RemoteArea source = RemoteArea.from(
                RemoteAreaCreateState.builder()
                        .province("강원특별자치도")
                        .district("양양군")
                        .village("강현면")
                        .subarea("정암리")
                        .regionType(ShippingAddressRegionType.DELIVERY_IMPOSSIBLE)
                        .build()
        );
        RemoteAreaJpaEntity entity = RemoteAreaJpaEntity.from(source);

        // when
        RemoteArea domain = RemoteAreaJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getRegionType()).isEqualTo(ShippingAddressRegionType.DELIVERY_IMPOSSIBLE);
    }
}
