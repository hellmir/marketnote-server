package com.personal.marketnote.user.domain.shippingaddress;

import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.common.domain.phonenumber.PhoneNumber;
import com.personal.marketnote.user.domain.shippingaddress.exception.DeliveryRequestMessageNoValueException;
import com.personal.marketnote.user.domain.shippingaddress.exception.InvalidDeliveryRequestMessageLengthException;
import com.personal.marketnote.user.domain.shippingaddress.exception.InvalidShippingAddressDeletionException;
import com.personal.marketnote.user.domain.shippingaddress.exception.ShippingAddressCompanyNameNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingAddressTest {

    @Test
    @DisplayName("ShippingAddressCreateState로 ShippingAddress를 생성하면 모든 필드가 매핑된다")
    void shouldCreateShippingAddressFromCreateState() {
        ShippingAddressCreateState state = createHomeAddressState();

        ShippingAddress address = ShippingAddress.from(state);

        assertThat(address.getUserId()).isEqualTo(1L);
        assertThat(address.getAddressType()).isEqualTo(ShippingAddressType.HOME);
        assertThat(address.getAddress()).isEqualTo("서울시 강남구");
        assertThat(address.isDefault()).isTrue();
    }

    @Test
    @DisplayName("회사 배송지에 회사명이 없으면 ShippingAddressCompanyNameNoValueException이 발생한다")
    void shouldThrowWhenCompanyNameMissingForCompanyType() {
        ShippingAddressCreateState state = ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.COMPANY)
                .address("서울시 강남구")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .isDefault(false)
                .build();

        assertThatThrownBy(() -> ShippingAddress.from(state))
                .isInstanceOf(ShippingAddressCompanyNameNoValueException.class);
    }

    @Test
    @DisplayName("집 배송지를 삭제하면 InvalidShippingAddressDeletionException이 발생한다")
    void shouldThrowWhenDeletingHomeAddress() {
        ShippingAddress address = ShippingAddress.from(createHomeAddressState());

        assertThatThrownBy(address::delete)
                .isInstanceOf(InvalidShippingAddressDeletionException.class);
    }

    @Test
    @DisplayName("기본 배송지를 삭제하면 InvalidShippingAddressDeletionException이 발생한다")
    void shouldThrowWhenDeletingDefaultAddress() {
        ShippingAddress address = ShippingAddress.from(ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 마포구")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .isDefault(true)
                .build());

        assertThatThrownBy(address::delete)
                .isInstanceOf(InvalidShippingAddressDeletionException.class);
    }

    @Test
    @DisplayName("기본이 아닌 기타 배송지를 삭제하면 비활성화된다")
    void shouldDeactivateWhenDeletingNonDefaultOtherAddress() {
        ShippingAddress address = ShippingAddress.from(ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.OTHER)
                .address("서울시 마포구")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .isDefault(false)
                .build());

        address.delete();

        assertThat(address.isInactive()).isTrue();
    }

    @Test
    @DisplayName("직접입력 배송 요청에 메시지가 없으면 DeliveryRequestMessageNoValueException이 발생한다")
    void shouldThrowWhenCustomDeliveryRequestWithoutMessage() {
        ShippingAddressCreateState state = ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .isDefault(false)
                .build();

        assertThatThrownBy(() -> ShippingAddress.from(state))
                .isInstanceOf(DeliveryRequestMessageNoValueException.class);
    }

    @Test
    @DisplayName("직접입력 배송 요청 메시지가 60자를 초과하면 InvalidDeliveryRequestMessageLengthException이 발생한다")
    void shouldThrowWhenCustomDeliveryRequestMessageExceeds60() {
        String longMessage = "가".repeat(61);
        ShippingAddressCreateState state = ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.CUSTOM)
                .deliveryRequestMessage(longMessage)
                .isDefault(false)
                .build();

        assertThatThrownBy(() -> ShippingAddress.from(state))
                .isInstanceOf(InvalidDeliveryRequestMessageLengthException.class);
    }

    @Test
    @DisplayName("update 시 직접입력 배송 요청 메시지가 정상이면 필드가 변경된다")
    void shouldUpdateFieldsSuccessfully() {
        ShippingAddress address = ShippingAddress.from(createHomeAddressState());

        address.update("서울시 서초구", "201호", null, "새 별칭",
                "김철수", PhoneNumber.of("010-9999-8888"), DeliveryRequestType.CUSTOM, "현관 비밀번호 1234");

        assertThat(address.getAddress()).isEqualTo("서울시 서초구");
        assertThat(address.getRecipientName()).isEqualTo("김철수");
        assertThat(address.getDeliveryRequestMessage()).isEqualTo("현관 비밀번호 1234");
    }

    private ShippingAddressCreateState createHomeAddressState() {
        return ShippingAddressCreateState.builder()
                .userId(1L)
                .addressType(ShippingAddressType.HOME)
                .address("서울시 강남구")
                .addressDetail("101호")
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .deliveryRequestType(DeliveryRequestType.LEAVE_AT_DOOR)
                .isDefault(true)
                .build();
    }
}
