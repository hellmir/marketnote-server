package com.personal.marketnote.user.adapter.out.mapper;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.domain.deliveryrequestmessage.DeliveryRequestMessage;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.common.domain.recipientname.RecipientName;
import com.personal.marketnote.user.adapter.out.persistence.shippingaddress.entity.ShippingAddressJpaEntity;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressCreateState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingAddressJpaEntityToDomainMapperTest {

    @Test
    @DisplayName("JPA 엔티티의 모든 필드를 도메인 객체로 매핑한다")
    void shouldMapAllFieldsToDomain() {
        // given - 도메인 객체로부터 JPA 엔티티 생성 후 ID는 ReflectionTestUtils로 주입
        ShippingAddress source = ShippingAddress.from(
                ShippingAddressCreateState.builder()
                        .userId(10L)
                        .addressType(ShippingAddressType.HOME)
                        .address("서울시 강남구")
                        .addressDetail("101동 202호")
                        .companyName(null)
                        .addressAlias("우리집")
                        .recipientName("홍길동")
                        .recipientPhoneNumber("01012345678")
                        .deliveryRequestType(DeliveryRequestType.CUSTOM)
                        .deliveryRequestMessage("문 앞에 놓아주세요")
                        .isDefault(true)
                        .regionType(ShippingAddressRegionType.NORMAL)
                        .build()
        );
        ShippingAddressJpaEntity entity = ShippingAddressJpaEntity.from(source);
        ReflectionTestUtils.setField(entity, "id", 99L);

        // when
        ShippingAddress domain = ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(99L);
        assertThat(domain.getUserId()).isEqualTo(10L);
        assertThat(domain.getAddressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(domain.getAddress()).isEqualTo("서울시 강남구");
        assertThat(domain.getAddressDetail()).isEqualTo("101동 202호");
        assertThat(domain.getCompanyName()).isNull();
        assertThat(domain.getAddressAlias()).isEqualTo("우리집");
        assertThat(domain.getRecipientName()).isEqualTo(RecipientName.of("홍길동"));
        assertThat(domain.getRecipientPhoneNumber()).isEqualTo(PhoneNumber.of("01012345678"));
        assertThat(domain.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.CUSTOM);
        assertThat(domain.getDeliveryRequestMessage()).isEqualTo(DeliveryRequestMessage.of("문 앞에 놓아주세요"));
        assertThat(domain.isDefault()).isTrue();
        assertThat(domain.getRegionType()).isEqualTo(ShippingAddressRegionType.NORMAL);
    }

    @Test
    @DisplayName("isDefault가 null이면 false로 매핑한다")
    void shouldMapNullIsDefaultAsFalse() {
        // given - JPA 엔티티의 isDefault에 null 직접 주입
        ShippingAddress source = ShippingAddress.from(
                ShippingAddressCreateState.builder()
                        .userId(11L)
                        .addressType(ShippingAddressType.OTHER)
                        .address("부산시 해운대구")
                        .addressDetail("33-44")
                        .companyName(null)
                        .addressAlias(null)
                        .recipientName("김철수")
                        .recipientPhoneNumber("01098765432")
                        .deliveryRequestType(DeliveryRequestType.NONE)
                        .deliveryRequestMessage(null)
                        .isDefault(false)
                        .regionType(ShippingAddressRegionType.JEJU)
                        .build()
        );
        ShippingAddressJpaEntity entity = ShippingAddressJpaEntity.from(source);
        ReflectionTestUtils.setField(entity, "isDefault", null);

        // when
        ShippingAddress domain = ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.isDefault()).isFalse();
    }

    @Test
    @DisplayName("deliveryRequestType이 CUSTOM이 아니면 deliveryRequestMessage가 null로 매핑된다")
    void shouldMapNullDeliveryRequestMessageWhenTypeIsNotCustom() {
        // given
        ShippingAddress source = ShippingAddress.from(
                ShippingAddressCreateState.builder()
                        .userId(20L)
                        .addressType(ShippingAddressType.OTHER)
                        .address("대전시 유성구")
                        .addressDetail("12-34")
                        .companyName(null)
                        .addressAlias(null)
                        .recipientName("이영희")
                        .recipientPhoneNumber("01055556666")
                        .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                        .deliveryRequestMessage(null)
                        .isDefault(false)
                        .regionType(ShippingAddressRegionType.NORMAL)
                        .build()
        );
        ShippingAddressJpaEntity entity = ShippingAddressJpaEntity.from(source);

        // when
        ShippingAddress domain = ShippingAddressJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getDeliveryRequestMessage()).isNull();
        assertThat(domain.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
    }
}
