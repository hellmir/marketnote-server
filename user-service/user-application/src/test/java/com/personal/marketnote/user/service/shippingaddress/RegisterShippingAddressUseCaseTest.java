package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressSnapshotState;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;
import com.personal.marketnote.user.port.out.shippingaddress.FindShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.SaveShippingAddressPort;
import com.personal.marketnote.user.port.out.shippingaddress.UpdateShippingAddressPort;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterShippingAddressUseCaseTest {
    @InjectMocks
    private RegisterShippingAddressService registerShippingAddressService;

    @Mock
    private FindShippingAddressPort findShippingAddressPort;

    @Mock
    private SaveShippingAddressPort saveShippingAddressPort;

    @Mock
    private UpdateShippingAddressPort updateShippingAddressPort;

    @Test
    @DisplayName("첫 번째 배송지 등록 시 기본 배송지로 설정된다")
    void registerShippingAddress_firstAddress_setsAsDefault() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(false);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(false);

        ShippingAddress savedShippingAddress = createShippingAddress(1L, userId, ShippingAddressType.HOME, true);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.addressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(result.isDefault()).isTrue();

        verify(findShippingAddressPort).existsByUserIdAndAddressType(userId, ShippingAddressType.HOME);
        verify(findShippingAddressPort).existsByUserId(userId);
        verify(findShippingAddressPort, never()).findDefaultsByUserId(userId);
        verify(saveShippingAddressPort).save(any(ShippingAddress.class));
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("isDefault=true로 등록 시 기존 기본 배송지가 해제된다")
    void registerShippingAddress_isDefaultTrue_unsetsExistingDefaults() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 마포구 월드컵로 456")
                .addressDetail("201동 502호")
                .addressAlias("친구 집")
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .isDefault(true)
                .build();

        when(findShippingAddressPort.countByUserIdAndAddressType(userId, ShippingAddressType.OTHER))
                .thenReturn(2L);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(true);

        ShippingAddress existingDefault = createShippingAddress(10L, userId, ShippingAddressType.HOME, true);
        when(findShippingAddressPort.findDefaultsByUserId(userId))
                .thenReturn(List.of(existingDefault));

        ShippingAddress savedShippingAddress = createShippingAddress(2L, userId, ShippingAddressType.OTHER, true);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.addressType()).isEqualTo(ShippingAddressType.OTHER);
        assertThat(result.isDefault()).isTrue();

        verify(findShippingAddressPort).countByUserIdAndAddressType(userId, ShippingAddressType.OTHER);
        verify(findShippingAddressPort).existsByUserId(userId);
        verify(findShippingAddressPort).findDefaultsByUserId(userId);
        verify(updateShippingAddressPort).update(existingDefault);
        verify(saveShippingAddressPort).save(any(ShippingAddress.class));
    }

    @Test
    @DisplayName("HOME 타입 배송지가 이미 존재하면 EntityExistsException이 발생한다")
    void registerShippingAddress_duplicateHome_throwsEntityExistsException() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registerShippingAddressService.registerShippingAddress(command))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("이미 등록된 집 배송지가 존재합니다");

        verify(findShippingAddressPort).existsByUserIdAndAddressType(userId, ShippingAddressType.HOME);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(saveShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("COMPANY 타입 배송지가 이미 존재하면 EntityExistsException이 발생한다")
    void registerShippingAddress_duplicateCompany_throwsEntityExistsException() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 서초구 반포대로 789")
                .addressDetail("5층")
                .companyName("테스트 회사")
                .recipientName("이영희")
                .recipientPhoneNumber("010-5555-6666")
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.COMPANY))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registerShippingAddressService.registerShippingAddress(command))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("이미 등록된 회사 배송지가 존재합니다");

        verify(findShippingAddressPort).existsByUserIdAndAddressType(userId, ShippingAddressType.COMPANY);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(saveShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("OTHER 타입 배송지가 5개 이상이면 IllegalArgumentException이 발생한다")
    void registerShippingAddress_otherLimitExceeded_throwsIllegalArgumentException() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 송파구 올림픽로 100")
                .addressDetail("301동 801호")
                .addressAlias("기타 주소")
                .recipientName("박민수")
                .recipientPhoneNumber("010-7777-8888")
                .isDefault(false)
                .build();

        when(findShippingAddressPort.countByUserIdAndAddressType(userId, ShippingAddressType.OTHER))
                .thenReturn(5L);

        // when & then
        assertThatThrownBy(() -> registerShippingAddressService.registerShippingAddress(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기타 배송지는 최대 5개까지 등록할 수 있습니다");

        verify(findShippingAddressPort).countByUserIdAndAddressType(userId, ShippingAddressType.OTHER);
        verifyNoMoreInteractions(findShippingAddressPort);
        verifyNoInteractions(saveShippingAddressPort, updateShippingAddressPort);
    }

    @Test
    @DisplayName("deliveryRequestType이 null이면 NONE으로 설정된다")
    void registerShippingAddress_nullDeliveryRequestType_setsToNone() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(null)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(false);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(false);

        ShippingAddress savedShippingAddress = createShippingAddress(1L, userId, ShippingAddressType.HOME, true);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(1L);

        verify(findShippingAddressPort).existsByUserIdAndAddressType(userId, ShippingAddressType.HOME);
        verify(findShippingAddressPort).existsByUserId(userId);
        verify(saveShippingAddressPort).save(any(ShippingAddress.class));
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("첫 번째 배송지가 아니고 isDefault가 false이면 기본 배송지가 되지 않는다")
    void registerShippingAddress_notFirstAndNotDefault_doesNotSetAsDefault() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 용산구 이태원로 200")
                .addressDetail("401동 1202호")
                .addressAlias("부모님 댁")
                .recipientName("홍부모")
                .recipientPhoneNumber("010-3333-4444")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_SECURITY)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.countByUserIdAndAddressType(userId, ShippingAddressType.OTHER))
                .thenReturn(1L);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(true);

        ShippingAddress savedShippingAddress = createShippingAddress(3L, userId, ShippingAddressType.OTHER, false);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.addressType()).isEqualTo(ShippingAddressType.OTHER);
        assertThat(result.isDefault()).isFalse();

        verify(findShippingAddressPort).countByUserIdAndAddressType(userId, ShippingAddressType.OTHER);
        verify(findShippingAddressPort).existsByUserId(userId);
        verify(findShippingAddressPort, never()).findDefaultsByUserId(userId);
        verify(saveShippingAddressPort).save(any(ShippingAddress.class));
        verifyNoInteractions(updateShippingAddressPort);
    }

    @Test
    @DisplayName("CUSTOM 타입 선택 시 배송 요청사항 메시지(최대 60자)가 정상 저장된다")
    void registerShippingAddress_customTypeWithValidMessage_savesWithMessage() {
        // given
        Long userId = 1L;
        String message = "a".repeat(60);
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(message)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(false);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(false);

        ShippingAddress savedShippingAddress = createShippingAddress(1L, userId, ShippingAddressType.HOME, true);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(1L);

        verify(saveShippingAddressPort).save(argThat(sa ->
                sa.getDeliveryRequestType() == DeliveryRequestType.CUSTOM
                        && message.equals(sa.getDeliveryRequestMessage())
        ));
    }

    @Test
    @DisplayName("CUSTOM 타입 선택 시 배송 요청사항 메시지가 60자를 초과하면 예외가 발생한다")
    void registerShippingAddress_customTypeWithExceedingMessage_throwsIllegalArgumentException() {
        // given
        Long userId = 1L;
        String message = "a".repeat(61);
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(message)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(false);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> registerShippingAddressService.registerShippingAddress(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("배송 요청사항 메시지는 최대 60자까지 입력할 수 있습니다");

        verifyNoInteractions(saveShippingAddressPort);
    }

    @Test
    @DisplayName("CUSTOM이 아닌 타입 선택 시 배송 요청사항 메시지가 무시된다")
    void registerShippingAddress_nonCustomTypeWithMessage_clearsMessage() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구 테헤란로 123")
                .addressDetail("101동 1001호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .deliveryRequestMessage("이 메시지는 무시되어야 합니다")
                .isDefault(false)
                .build();

        when(findShippingAddressPort.existsByUserIdAndAddressType(userId, ShippingAddressType.HOME))
                .thenReturn(false);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(false);

        ShippingAddress savedShippingAddress = createShippingAddress(1L, userId, ShippingAddressType.HOME, true);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        registerShippingAddressService.registerShippingAddress(command);

        // then
        verify(saveShippingAddressPort).save(argThat(sa ->
                sa.getDeliveryRequestType() == DeliveryRequestType.LEAVE_AT_DOOR
                        && sa.getDeliveryRequestMessage() == null
        ));
    }

    @Test
    @DisplayName("OTHER 타입 배송지 등록 시 주소 별명 없이도 정상 등록된다")
    void registerShippingAddress_otherTypeWithoutAlias_succeedsWithoutAlias() {
        // given
        Long userId = 1L;
        RegisterShippingAddressCommand command = RegisterShippingAddressCommand.builder()
                .userId(userId)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 마포구 월드컵로 456")
                .addressDetail("201동 502호")
                .addressAlias(null)
                .recipientName("김철수")
                .recipientPhoneNumber("010-9876-5432")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .isDefault(false)
                .build();

        when(findShippingAddressPort.countByUserIdAndAddressType(userId, ShippingAddressType.OTHER))
                .thenReturn(1L);
        when(findShippingAddressPort.existsByUserId(userId))
                .thenReturn(true);

        ShippingAddress savedShippingAddress = createShippingAddress(5L, userId, ShippingAddressType.OTHER, false);
        when(saveShippingAddressPort.save(any(ShippingAddress.class)))
                .thenReturn(savedShippingAddress);

        // when
        RegisterShippingAddressResult result = registerShippingAddressService.registerShippingAddress(command);

        // then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.addressType()).isEqualTo(ShippingAddressType.OTHER);

        verify(saveShippingAddressPort).save(any(ShippingAddress.class));
    }

    private ShippingAddress createShippingAddress(
            Long id,
            Long userId,
            ShippingAddressType addressType,
            boolean isDefault
    ) {
        return ShippingAddress.from(
                ShippingAddressSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .addressType(addressType)
                        .address("서울시 강남구 테헤란로 123")
                        .addressDetail("101동 1001호")
                        .recipientName("홍길동")
                        .recipientPhoneNumber("010-1234-5678")
                        .deliveryRequestType(DeliveryRequestType.NONE)
                        .isDefault(isDefault)
                        .build()
        );
    }
}
