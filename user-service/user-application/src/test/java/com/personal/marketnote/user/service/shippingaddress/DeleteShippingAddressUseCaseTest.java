package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.out.event.PublishShippingAddressEventPort;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteShippingAddressUseCaseTest {
    @InjectMocks
    private DeleteShippingAddressService deleteShippingAddressService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Mock
    private UpdateShippingAddressPort updateShippingAddressPort;

    @Mock
    private PublishShippingAddressEventPort publishShippingAddressEventPort;

    @Test
    @DisplayName("배송지가 존재하면 논리적 삭제한다")
    void deleteShippingAddress_existingOtherAddress_deactivates() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("4층")
                .addressAlias("사무실")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(false)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        deleteShippingAddressService.deleteShippingAddress(shippingAddressId, userId);

        // then
        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(updateShippingAddressPort).update(shippingAddress);
        verify(publishShippingAddressEventPort).publishShippingAddressChangedEvent(
                1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "4층", ShippingAddressChangeAction.DELETED
        );
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
    void deleteShippingAddress_notFound_throwsShippingAddressNotFoundException() {
        // given
        Long shippingAddressId = 999L;
        Long userId = 100L;

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> deleteShippingAddressService.deleteShippingAddress(shippingAddressId, userId))
                .isInstanceOf(ShippingAddressNotFoundException.class)
                .hasMessageContaining(String.valueOf(shippingAddressId));

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort, publishShippingAddressEventPort);
    }

    @Test
    @DisplayName("HOME 타입 배송지는 삭제할 수 없다")
    void deleteShippingAddress_homeType_throwsIllegalArgumentException() {
        // given
        Long shippingAddressId = 2L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 456")
                .addressDetail("101호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(false)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // expect
        assertThatThrownBy(() -> deleteShippingAddressService.deleteShippingAddress(shippingAddressId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("집 배송지는 삭제할 수 없습니다.");

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort, publishShippingAddressEventPort);
    }

    @Test
    @DisplayName("기본 배송지는 삭제할 수 없다")
    void deleteShippingAddress_defaultAddress_throwsIllegalArgumentException() {
        // given
        Long shippingAddressId = 3L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 서초구 서초대로 789")
                .addressDetail("5층")
                .companyName("오포니티")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(true)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // expect
        assertThatThrownBy(() -> deleteShippingAddressService.deleteShippingAddress(shippingAddressId, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("기본 배송지는 삭제할 수 없습니다. 다른 배송지를 기본으로 설정한 후 삭제해주세요.");

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort, publishShippingAddressEventPort);
    }
}
