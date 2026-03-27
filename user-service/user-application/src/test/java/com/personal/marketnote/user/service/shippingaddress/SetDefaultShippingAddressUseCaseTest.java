package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SetDefaultShippingAddressUseCaseTest {
    @InjectMocks
    private SetDefaultShippingAddressService setDefaultShippingAddressService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Mock
    private UpdateShippingAddressPort updateShippingAddressPort;

    @Test
    @DisplayName("대상 배송지를 기본 배송지로 설정하고 기존 기본 배송지를 해제한다")
    void setDefaultShippingAddress_success_setsTargetAndUnsetsCurrentDefault() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress targetAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(false)
                .build());

        ShippingAddress currentDefault = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(2L)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 서초구 서초대로 456")
                .addressDetail("5층")
                .companyName("테스트 회사")
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .isDefault(true)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(targetAddress));
        when(findShippingAddressPort.findDefaultsByUserId(userId))
                .thenReturn(List.of(currentDefault));

        // when
        setDefaultShippingAddressService.setDefaultShippingAddress(shippingAddressId, userId);

        // then
        assertThat(currentDefault.isDefault()).isFalse();
        assertThat(targetAddress.isDefault()).isTrue();

        InOrder inOrder = inOrder(findShippingAddressPort, updateShippingAddressPort);
        inOrder.verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        inOrder.verify(findShippingAddressPort).findDefaultsByUserId(userId);
        inOrder.verify(updateShippingAddressPort).update(currentDefault);
        inOrder.verify(updateShippingAddressPort).update(targetAddress);
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("이미 기본 배송지인 경우 아무 작업 없이 반환한다")
    void setDefaultShippingAddress_alreadyDefault_returnsWithoutUpdate() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress targetAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(true)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(targetAddress));

        // when
        setDefaultShippingAddressService.setDefaultShippingAddress(shippingAddressId, userId);

        // then
        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(findShippingAddressPort, never()).findDefaultsByUserId(userId);
        verifyNoInteractions(updateShippingAddressPort);
        verifyNoMoreInteractions(findShippingAddressPort);
    }

    @Test
    @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
    void setDefaultShippingAddress_notFound_throwsShippingAddressNotFoundException() {
        // given
        Long shippingAddressId = 999L;
        Long userId = 100L;

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() ->
                setDefaultShippingAddressService.setDefaultShippingAddress(shippingAddressId, userId))
                .isInstanceOf(ShippingAddressNotFoundException.class)
                .hasMessage("배송지를 찾을 수 없습니다. 전송된 배송지 ID: " + shippingAddressId);

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("기존 기본 배송지가 여러 개인 경우 모두 해제된다")
    void setDefaultShippingAddress_multipleDefaults_unsetsAll() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress targetAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .isDefault(false)
                .build());

        ShippingAddress defaultAddress1 = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(2L)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 서초구 서초대로 456")
                .addressDetail("5층")
                .companyName("테스트 회사")
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .isDefault(true)
                .build());

        ShippingAddress defaultAddress2 = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(3L)
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 마포구 홍대로 789")
                .addressDetail("3층")
                .addressAlias("친구 집")
                .recipientName("이영희")
                .recipientPhoneNumber("010-5555-6666")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .isDefault(true)
                .build());

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(targetAddress));
        when(findShippingAddressPort.findDefaultsByUserId(userId))
                .thenReturn(List.of(defaultAddress1, defaultAddress2));

        // when
        setDefaultShippingAddressService.setDefaultShippingAddress(shippingAddressId, userId);

        // then
        assertThat(defaultAddress1.isDefault()).isFalse();
        assertThat(defaultAddress2.isDefault()).isFalse();
        assertThat(targetAddress.isDefault()).isTrue();

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(findShippingAddressPort).findDefaultsByUserId(userId);
        verify(updateShippingAddressPort).update(defaultAddress1);
        verify(updateShippingAddressPort).update(defaultAddress2);
        verify(updateShippingAddressPort).update(targetAddress);
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }
}
