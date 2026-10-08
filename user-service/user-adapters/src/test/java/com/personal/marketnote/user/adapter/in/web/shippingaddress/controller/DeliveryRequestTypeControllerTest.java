package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetDeliveryRequestTypesResponse;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetDeliveryRequestTypesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryRequestTypeController 배송 요청사항 조회")
class DeliveryRequestTypeControllerTest {

    @InjectMocks
    private DeliveryRequestTypeController controller;

    @Mock
    private GetDeliveryRequestTypesUseCase getDeliveryRequestTypesUseCase;

    @Test
    @DisplayName("배송 요청사항 목록을 조회하면 UseCase 결과를 응답 리스트로 매핑하여 OK를 반환한다")
    void returnsDeliveryRequestTypeList() {
        // given
        List<GetDeliveryRequestTypesResult> results = List.of(
                new GetDeliveryRequestTypesResult(DeliveryRequestType.NONE, DeliveryRequestType.NONE.getDescription()),
                new GetDeliveryRequestTypesResult(
                        DeliveryRequestType.LEAVE_AT_DOOR, DeliveryRequestType.LEAVE_AT_DOOR.getDescription()
                ),
                new GetDeliveryRequestTypesResult(
                        DeliveryRequestType.CUSTOM, DeliveryRequestType.CUSTOM.getDescription()
                )
        );
        when(getDeliveryRequestTypesUseCase.getDeliveryRequestTypes()).thenReturn(results);

        // when
        ResponseEntity<BaseResponse<List<GetDeliveryRequestTypesResponse>>> response =
                controller.getDeliveryRequestTypes();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(3);
        verify(getDeliveryRequestTypesUseCase).getDeliveryRequestTypes();
    }

    @Test
    @DisplayName("빈 목록도 OK 응답으로 래핑된다")
    void returnsEmptyListAsOk() {
        // given
        when(getDeliveryRequestTypesUseCase.getDeliveryRequestTypes()).thenReturn(List.of());

        // when
        ResponseEntity<BaseResponse<List<GetDeliveryRequestTypesResponse>>> response =
                controller.getDeliveryRequestTypes();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
    }
}
