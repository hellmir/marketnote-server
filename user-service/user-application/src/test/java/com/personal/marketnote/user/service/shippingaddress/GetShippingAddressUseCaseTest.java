package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetShippingAddressUseCaseTest {
    @InjectMocks
    private GetShippingAddressService getShippingAddressService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Test
    @DisplayName("배송지가 존재하면 배송지 정보를 반환한다")
    void getShippingAddress_exists_returnsResult() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(
                ShippingAddressSnapshotState.builder()
                        .id(shippingAddressId)
                        .userId(userId)
                        .addressType(ShippingAddressType.HOME)
                        .address("서울특별시 강남구 테헤란로 123")
                        .addressDetail("456호")
                        .companyName(null)
                        .addressAlias(null)
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1234-5678")
                        .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                        .deliveryRequestMessage("문 앞에 놓아주세요")
                        .isDefault(true)
                        .regionType(ShippingAddressRegionType.NORMAL)
                        .build()
        );

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        GetShippingAddressResult result = getShippingAddressService.getShippingAddress(shippingAddressId, userId);

        // then
        assertThat(result.id()).isEqualTo(shippingAddressId);
        assertThat(result.addressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(result.address()).isEqualTo("서울특별시 강남구 테헤란로 123");
        assertThat(result.addressDetail()).isEqualTo("456호");
        assertThat(result.companyName()).isNull();
        assertThat(result.addressAlias()).isNull();
        assertThat(result.recipientName()).isEqualTo("홍길동");
        assertThat(result.recipientPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(result.deliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
        assertThat(result.deliveryRequestMessage()).isEqualTo("문 앞에 놓아주세요");
        assertThat(result.isDefault()).isTrue();

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
    }

    @Test
    @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
    void getShippingAddress_notExists_throwsShippingAddressNotFoundException() {
        // given
        Long shippingAddressId = 999L;
        Long userId = 100L;

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> getShippingAddressService.getShippingAddress(shippingAddressId, userId))
                .isInstanceOf(ShippingAddressNotFoundException.class)
                .hasMessage(String.format("배송지를 찾을 수 없습니다. 전송된 배송지 ID: %d", shippingAddressId));

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
    }
}
