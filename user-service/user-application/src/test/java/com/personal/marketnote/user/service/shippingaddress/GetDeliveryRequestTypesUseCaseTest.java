package com.personal.marketnote.user.service.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.DeliveryRequestType;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GetDeliveryRequestTypesUseCaseTest {
    @InjectMocks
    private GetDeliveryRequestTypesService getDeliveryRequestTypesService;

    @Test
    @DisplayName("배송 요청사항 목록을 조회하면 모든 타입이 반환된다")
    void getDeliveryRequestTypes_returnsAllTypes() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        assertThat(results).hasSize(DeliveryRequestType.values().length);
    }

    @Test
    @DisplayName("배송 요청사항 목록의 순서가 디자인 기준과 일치한다")
    void getDeliveryRequestTypes_orderMatchesDesign() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        assertThat(results.get(0).type()).isEqualTo(DeliveryRequestType.NONE);
        assertThat(results.get(1).type()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
        assertThat(results.get(2).type()).isEqualTo(DeliveryRequestType.RECEIVE_OR_LEAVE_AT_DOOR);
        assertThat(results.get(3).type()).isEqualTo(DeliveryRequestType.LEAVE_AT_SECURITY);
        assertThat(results.get(4).type()).isEqualTo(DeliveryRequestType.LEAVE_AT_DELIVERY_BOX);
        assertThat(results.get(5).type()).isEqualTo(DeliveryRequestType.CUSTOM);
    }

    @Test
    @DisplayName("각 배송 요청사항 타입에 description이 포함되어 있다")
    void getDeliveryRequestTypes_allHaveDescription() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        results.forEach(result ->
                assertThat(result.description()).isNotNull().isNotBlank()
        );
    }

    @Test
    @DisplayName("NONE 타입의 description은 선택 안 함이다")
    void getDeliveryRequestTypes_noneDescription() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        GetDeliveryRequestTypesResult noneResult = results.stream()
                .filter(result -> result.type() == DeliveryRequestType.NONE)
                .findFirst()
                .orElseThrow();
        assertThat(noneResult.description()).isEqualTo("선택 안 함");
    }

    @Test
    @DisplayName("CUSTOM 타입의 description은 직접 입력이다")
    void getDeliveryRequestTypes_customDescription() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        GetDeliveryRequestTypesResult customResult = results.stream()
                .filter(result -> result.type() == DeliveryRequestType.CUSTOM)
                .findFirst()
                .orElseThrow();
        assertThat(customResult.description()).isEqualTo("직접 입력");
    }

    @Test
    @DisplayName("RECEIVE_OR_LEAVE_AT_DOOR 타입의 description은 직접 받고 부재시 문 앞에 놓아 주세요이다")
    void getDeliveryRequestTypes_receiveOrLeaveAtDoorDescription() {
        // when
        List<GetDeliveryRequestTypesResult> results = getDeliveryRequestTypesService.getDeliveryRequestTypes();

        // then
        GetDeliveryRequestTypesResult result = results.stream()
                .filter(r -> r.type() == DeliveryRequestType.RECEIVE_OR_LEAVE_AT_DOOR)
                .findFirst()
                .orElseThrow();
        assertThat(result.description()).isEqualTo("직접 받고 부재시 문 앞에 놓아 주세요");
    }
}
