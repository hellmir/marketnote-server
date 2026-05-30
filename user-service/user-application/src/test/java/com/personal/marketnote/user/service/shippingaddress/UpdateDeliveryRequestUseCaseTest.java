package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.domain.shippingaddress.exception.DeliveryRequestMessageNoValueException;
import com.personal.marketnote.user.domain.shippingaddress.exception.InvalidDeliveryRequestMessageLengthException;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateDeliveryRequestCommand;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
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
class UpdateDeliveryRequestUseCaseTest {
    @InjectMocks
    private UpdateDeliveryRequestService updateDeliveryRequestService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Mock
    private UpdateShippingAddressPort updateShippingAddressPort;

    @Test
    @DisplayName("프리셋 배송 요청 타입으로 업데이트하면 배송지의 배송 요청사항이 변경된다")
    void updateDeliveryRequest_presetType_updatesDeliveryRequest() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
        assertThat(shippingAddress.getDeliveryRequestMessage()).isNull();

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(updateShippingAddressPort).update(shippingAddress);
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("CUSTOM 타입으로 업데이트 시 배송 요청사항 메시지가 유지된다")
    void updateDeliveryRequest_customTypeWithMessage_keepsMessage() {
        // given
        Long shippingAddressId = 2L;
        Long userId = 200L;
        String message = "현관 비밀번호 1234 입니다";

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(message)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.CUSTOM);
        assertThat(shippingAddress.getDeliveryRequestMessage()).isEqualTo(message);

        verify(updateShippingAddressPort).update(shippingAddress);
    }

    @Test
    @DisplayName("CUSTOM이 아닌 타입으로 업데이트 시 기존 배송 요청사항 메시지가 null로 클리어된다")
    void updateDeliveryRequest_nonCustomType_clearsMessage() {
        // given
        Long shippingAddressId = 3L;
        Long userId = 300L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage("기존 요청사항")
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .deliveryRequestMessage("이 메시지는 무시되어야 합니다")
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_SECURITY);
        assertThat(shippingAddress.getDeliveryRequestMessage()).isNull();

        verify(updateShippingAddressPort).update(shippingAddress);
    }

    @Test
    @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
    void updateDeliveryRequest_nonExistingAddress_throwsShippingAddressNotFoundException() {
        // given
        Long shippingAddressId = 999L;
        Long userId = 100L;

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command))
                .isInstanceOf(ShippingAddressNotFoundException.class)
                .hasMessage("배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999");

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("CUSTOM 타입인데 메시지가 없으면 DeliveryRequestMessageNoValueException이 발생한다")
    void updateDeliveryRequest_customTypeWithoutMessage_throwsException() {
        // given
        Long shippingAddressId = 4L;
        Long userId = 400L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when & then
        assertThatThrownBy(() -> updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command))
                .isInstanceOf(DeliveryRequestMessageNoValueException.class);

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("CUSTOM 타입인데 메시지가 60자를 초과하면 InvalidDeliveryRequestMessageLengthException이 발생한다")
    void updateDeliveryRequest_customTypeWithLongMessage_throwsException() {
        // given
        Long shippingAddressId = 5L;
        Long userId = 500L;
        String longMessage = "가".repeat(61);

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .regionType(ShippingAddressRegionType.NORMAL)
                .build());

        UpdateDeliveryRequestCommand command = UpdateDeliveryRequestCommand.builder()
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(longMessage)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when & then
        assertThatThrownBy(() -> updateDeliveryRequestService.updateDeliveryRequest(shippingAddressId, userId, command))
                .isInstanceOf(InvalidDeliveryRequestMessageLengthException.class);

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoInteractions(updateShippingAddressPort);
    }
}
