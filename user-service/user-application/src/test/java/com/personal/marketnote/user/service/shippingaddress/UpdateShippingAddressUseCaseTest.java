package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.kafka.event.ShippingAddressChangeAction;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateShippingAddressCommand;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateShippingAddressUseCaseTest {
    @InjectMocks
    private UpdateShippingAddressService updateShippingAddressService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Mock
    private UpdateShippingAddressPort updateShippingAddressPort;

    @Mock
    private PublishShippingAddressEventPort publishShippingAddressEventPort;

    @Test
    @DisplayName("배송지가 존재하면 수정된 정보로 업데이트한다")
    void updateShippingAddress_existingAddress_updatesWithNewInfo() {
        // given
        Long shippingAddressId = 1L;
        Long userId = 100L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .companyName(null)
                .addressAlias(null)
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .isDefault(true)
                .build());

        UpdateShippingAddressCommand command = UpdateShippingAddressCommand.builder()
                .address("서울시 서초구 서초대로 456")
                .addressDetail("202동 303호")
                .companyName(null)
                .addressAlias(null)
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateShippingAddressService.updateShippingAddress(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getAddress()).isEqualTo("서울시 서초구 서초대로 456");
        assertThat(shippingAddress.getAddressDetail()).isEqualTo("202동 303호");
        assertThat(shippingAddress.getRecipientName()).isEqualTo("김철수");
        assertThat(shippingAddress.getRecipientPhoneNumber()).isEqualTo("010-9876-5432");
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(updateShippingAddressPort).update(shippingAddress);
        verify(publishShippingAddressEventPort).publishShippingAddressChangedEvent(
                1L, 100L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", ShippingAddressChangeAction.UPDATED
        );
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("배송지가 존재하지 않으면 ShippingAddressNotFoundException이 발생한다")
    void updateShippingAddress_nonExistingAddress_throwsShippingAddressNotFoundException() {
        // given
        Long shippingAddressId = 999L;
        Long userId = 100L;

        UpdateShippingAddressCommand command = UpdateShippingAddressCommand.builder()
                .address("서울시 서초구 서초대로 456")
                .addressDetail("202동 303호")
                .companyName(null)
                .addressAlias(null)
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.NONE)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updateShippingAddressService.updateShippingAddress(shippingAddressId, userId, command))
                .isInstanceOf(ShippingAddressNotFoundException.class)
                .hasMessage("배송지를 찾을 수 없습니다. 전송된 배송지 ID: 999");

        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(updateShippingAddressPort, publishShippingAddressEventPort);
    }

    @Test
    @DisplayName("CUSTOM 타입으로 수정 시 배송 요청사항 메시지가 유지된다")
    void updateShippingAddress_customTypeWithMessage_keepsMessage() {
        // given
        Long shippingAddressId = 3L;
        Long userId = 300L;
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
                .build());

        UpdateShippingAddressCommand command = UpdateShippingAddressCommand.builder()
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(message)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateShippingAddressService.updateShippingAddress(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.CUSTOM);
        assertThat(shippingAddress.getDeliveryRequestMessage()).isEqualTo(message);

        verify(updateShippingAddressPort).update(shippingAddress);
        verify(publishShippingAddressEventPort).publishShippingAddressChangedEvent(
                3L, 300L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", ShippingAddressChangeAction.UPDATED
        );
    }

    @Test
    @DisplayName("CUSTOM이 아닌 타입으로 수정 시 배송 요청사항 메시지가 무시된다")
    void updateShippingAddress_nonCustomTypeWithMessage_clearsMessage() {
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
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage("기존 요청사항")
                .isDefault(true)
                .build());

        UpdateShippingAddressCommand command = UpdateShippingAddressCommand.builder()
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 201호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage("이 메시지는 무시되어야 합니다")
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateShippingAddressService.updateShippingAddress(shippingAddressId, userId, command);

        // then
        assertThat(shippingAddress.getDeliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
        assertThat(shippingAddress.getDeliveryRequestMessage()).isNull();

        verify(updateShippingAddressPort).update(shippingAddress);
        verify(publishShippingAddressEventPort).publishShippingAddressChangedEvent(
                4L, 400L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", ShippingAddressChangeAction.UPDATED
        );
    }

    @Test
    @DisplayName("수정 후 updateShippingAddressPort.update가 호출된다")
    void updateShippingAddress_afterUpdate_callsUpdatePort() {
        // given
        Long shippingAddressId = 2L;
        Long userId = 200L;

        ShippingAddress shippingAddress = ShippingAddress.from(ShippingAddressSnapshotState.builder()
                .id(shippingAddressId)
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 강남구 역삼로 789")
                .addressDetail("5층")
                .companyName("오포니티")
                .addressAlias(null)
                .recipientName("이영희")
                .recipientPhoneNumber("010-5555-6666")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .deliveryRequestMessage(null)
                .isDefault(false)
                .build());

        UpdateShippingAddressCommand command = UpdateShippingAddressCommand.builder()
                .address("서울시 강남구 선릉로 100")
                .addressDetail("10층")
                .companyName("뉴트리캐시")
                .addressAlias(null)
                .recipientName("박민수")
                .recipientPhoneNumber("010-7777-8888")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .deliveryRequestMessage(null)
                .build();

        when(findShippingAddressPort.findByIdAndUserId(shippingAddressId, userId))
                .thenReturn(Optional.of(shippingAddress));

        // when
        updateShippingAddressService.updateShippingAddress(shippingAddressId, userId, command);

        // then
        verify(findShippingAddressPort).findByIdAndUserId(shippingAddressId, userId);
        verify(updateShippingAddressPort).update(shippingAddress);
        verify(publishShippingAddressEventPort).publishShippingAddressChangedEvent(
                2L, 200L, "박민수", "010-7777-8888", "서울시 강남구 선릉로 100", ShippingAddressChangeAction.UPDATED
        );
        verifyNoMoreInteractions(findShippingAddressPort, updateShippingAddressPort);
    }
}
