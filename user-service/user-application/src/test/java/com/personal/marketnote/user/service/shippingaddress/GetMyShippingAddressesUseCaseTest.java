package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressResult;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
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
class GetMyShippingAddressesUseCaseTest {
    @InjectMocks
    private GetMyShippingAddressesService getMyShippingAddressesService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Test
    @DisplayName("배송지 목록을 기본 배송지 우선, 주소 타입 순서로 정렬하여 반환한다")
    void getMyShippingAddresses_multipleAddresses_returnsSortedByDefaultAndAddressType() {
        // given
        Long userId = 1L;

        ShippingAddress otherNonDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(1L)
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("301호")
                .companyName(null)
                .addressAlias("친구 집")
                .recipientName("김친구")
                .recipientPhoneNumber("010-1111-1111")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(false)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        ShippingAddress homeNonDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(2L)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 마포구 월드컵북로 456")
                .addressDetail("101호")
                .companyName(null)
                .addressAlias(null)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-2222-2222")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage(null)
                .isDefault(false)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        ShippingAddress companyDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(3L)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 강남구 역삼로 789")
                .addressDetail("15층")
                .companyName("오포니티")
                .addressAlias(null)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-3333-3333")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        // 의도적으로 정렬되지 않은 순서로 제공: OTHER(비기본) → HOME(비기본) → COMPANY(기본)
        when(findShippingAddressPort.findAllByUserId(userId))
                .thenReturn(List.of(otherNonDefault, homeNonDefault, companyDefault));

        // when
        GetMyShippingAddressesResult result = getMyShippingAddressesService.getMyShippingAddresses(userId);

        // then
        List<GetMyShippingAddressResult> addresses = result.shippingAddresses();
        assertThat(addresses).hasSize(3);

        // 1번째: 기본 배송지 (COMPANY, isDefault=true)
        assertThat(addresses.get(0).id()).isEqualTo(3L);
        assertThat(addresses.get(0).addressType()).isEqualTo(ShippingAddressType.COMPANY);
        assertThat(addresses.get(0).isDefault()).isTrue();

        // 2번째: HOME (ordinal=0, isDefault=false)
        assertThat(addresses.get(1).id()).isEqualTo(2L);
        assertThat(addresses.get(1).addressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(addresses.get(1).isDefault()).isFalse();

        // 3번째: OTHER (ordinal=2, isDefault=false)
        assertThat(addresses.get(2).id()).isEqualTo(1L);
        assertThat(addresses.get(2).addressType()).isEqualTo(ShippingAddressType.OTHER);
        assertThat(addresses.get(2).isDefault()).isFalse();

        verify(findShippingAddressPort).findAllByUserId(userId);
        verifyNoMoreInteractions(findShippingAddressPort);
    }

    @Test
    @DisplayName("배송지가 없으면 빈 목록을 반환한다")
    void getMyShippingAddresses_noAddresses_returnsEmptyList() {
        // given
        Long userId = 2L;

        when(findShippingAddressPort.findAllByUserId(userId))
                .thenReturn(List.of());

        // when
        GetMyShippingAddressesResult result = getMyShippingAddressesService.getMyShippingAddresses(userId);

        // then
        assertThat(result.shippingAddresses()).isEmpty();

        verify(findShippingAddressPort).findAllByUserId(userId);
        verifyNoMoreInteractions(findShippingAddressPort);
    }

    @Test
    @DisplayName("기본 배송지가 여러 타입 중 하나일 때 기본 배송지가 첫 번째로 정렬된다")
    void getMyShippingAddresses_defaultAmongMultipleTypes_defaultComesFirst() {
        // given
        Long userId = 3L;

        ShippingAddress homeDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(10L)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 송파구 올림픽로 100")
                .addressDetail("201호")
                .companyName(null)
                .addressAlias(null)
                .recipientName("이영희")
                .recipientPhoneNumber("010-4444-4444")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        ShippingAddress companyNonDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(11L)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 중구 을지로 200")
                .addressDetail("10층")
                .companyName("테스트 회사")
                .addressAlias(null)
                .recipientName("이영희")
                .recipientPhoneNumber("010-5555-5555")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(false)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        ShippingAddress otherNonDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(12L)
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 용산구 이태원로 300")
                .addressDetail("501호")
                .companyName(null)
                .addressAlias("부모님 집")
                .recipientName("이영희")
                .recipientPhoneNumber("010-6666-6666")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DELIVERY_BOX)
                .deliveryRequestMessage(null)
                .isDefault(false)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        // 의도적으로 기본 배송지를 마지막에 배치: COMPANY(비기본) → OTHER(비기본) → HOME(기본)
        when(findShippingAddressPort.findAllByUserId(userId))
                .thenReturn(List.of(companyNonDefault, otherNonDefault, homeDefault));

        // when
        GetMyShippingAddressesResult result = getMyShippingAddressesService.getMyShippingAddresses(userId);

        // then
        List<GetMyShippingAddressResult> addresses = result.shippingAddresses();
        assertThat(addresses).hasSize(3);

        // 1번째: 기본 배송지 (HOME, isDefault=true)
        assertThat(addresses.get(0).id()).isEqualTo(10L);
        assertThat(addresses.get(0).addressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(addresses.get(0).isDefault()).isTrue();

        // 2번째: COMPANY (ordinal=1, isDefault=false)
        assertThat(addresses.get(1).id()).isEqualTo(11L);
        assertThat(addresses.get(1).addressType()).isEqualTo(ShippingAddressType.COMPANY);
        assertThat(addresses.get(1).isDefault()).isFalse();

        // 3번째: OTHER (ordinal=2, isDefault=false)
        assertThat(addresses.get(2).id()).isEqualTo(12L);
        assertThat(addresses.get(2).addressType()).isEqualTo(ShippingAddressType.OTHER);
        assertThat(addresses.get(2).isDefault()).isFalse();

        verify(findShippingAddressPort).findAllByUserId(userId);
        verifyNoMoreInteractions(findShippingAddressPort);
    }
}
