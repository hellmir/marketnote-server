package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentSupplierRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentSuppliersResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentSupplierResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.UpdateFulfillmentSupplierResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSuppliersUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentSupplierUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentSupplierUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentSupplierController 풀필먼트 공급사 관리")
class FulfillmentSupplierControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentSupplierController controller;

    @Mock
    private RegisterFulfillmentSupplierUseCase registerFulfillmentSupplierUseCase;
    @Mock
    private GetFulfillmentSuppliersUseCase getFulfillmentSuppliersUseCase;
    @Mock
    private UpdateFulfillmentSupplierUseCase updateFulfillmentSupplierUseCase;

    private RegisterFulfillmentSupplierRequest registerRequest() {
        RegisterFulfillmentSupplierRequest request = new RegisterFulfillmentSupplierRequest();
        ReflectionTestUtils.setField(request, "supplierName", "공급사1");
        return request;
    }

    private UpdateFulfillmentSupplierRequest updateRequest() {
        UpdateFulfillmentSupplierRequest request = new UpdateFulfillmentSupplierRequest();
        ReflectionTestUtils.setField(request, "supplierCode", "SUPP-01");
        ReflectionTestUtils.setField(request, "supplierName", "공급사1");
        return request;
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/suppliers/{customerCode} - 공급사 등록")
    class RegisterSupplier {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentSupplierUseCase.registerSupplier(any(RegisterFulfillmentSupplierCommand.class)))
                    .thenReturn(RegisterFulfillmentSupplierResult.of("ok", "SUC", "SUPP-01"));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentSupplierResponse>> response =
                    controller.registerSupplier(CUSTOMER_CODE, ACCESS_TOKEN, registerRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentSupplierUseCase).registerSupplier(any(RegisterFulfillmentSupplierCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/suppliers/{customerCode} - 공급사 목록 조회")
    class GetSuppliers {

        @Test
        @DisplayName("경로/헤더를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentSuppliersUseCase.getSuppliers(any(GetFulfillmentSuppliersCommand.class)))
                    .thenReturn(GetFulfillmentSuppliersResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentSuppliersResponse>> response =
                    controller.getSuppliers(CUSTOMER_CODE, ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentSuppliersUseCase).getSuppliers(any(GetFulfillmentSuppliersCommand.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vendors/fassto/suppliers/{customerCode} - 공급사 수정")
    class UpdateSupplier {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentSupplierUseCase.updateSupplier(any(UpdateFulfillmentSupplierCommand.class)))
                    .thenReturn(UpdateFulfillmentSupplierResult.of("ok", "SUC", "SUPP-01"));

            // when
            ResponseEntity<BaseResponse<UpdateFulfillmentSupplierResponse>> response =
                    controller.updateSupplier(CUSTOMER_CODE, ACCESS_TOKEN, updateRequest());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentSupplierUseCase).updateSupplier(any(UpdateFulfillmentSupplierCommand.class));
        }
    }
}
