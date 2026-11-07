package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentSupplierPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 공급사 수정 테스트")
class UpdateFulfillmentSupplierUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentSupplierService service;

    @Mock
    private UpdateFulfillmentSupplierPort updateFulfillmentSupplierPort;

    @Test
    @DisplayName("공급사 수정 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        UpdateFulfillmentSupplierCommand command = UpdateFulfillmentSupplierCommand.builder()
                .customerCode("CUST001")
                .accessToken("token")
                .supplierCode("SUP001")
                .supplierName("공급사명")
                .customerSupplierCode("CST01")
                .useYn("Y")
                .dealStartDate("20260401")
                .dealEndDate("20270401")
                .zipCode("12345")
                .address1("서울시")
                .address2("강남구")
                .ceoName("대표자")
                .businessNumber("123-45-67890")
                .businessCategory("도매")
                .businessType("식품")
                .phoneNumber("01012345678")
                .faxNumber("0212345678")
                .primaryManagerName("담당자1")
                .primaryManagerPosition("매니저")
                .primaryManagerPhoneNumber("01011111111")
                .primaryManagerEmail("emp1@test.com")
                .secondaryManagerName("담당자2")
                .secondaryManagerPosition("부매니저")
                .secondaryManagerPhoneNumber("01022222222")
                .secondaryManagerEmail("emp2@test.com")
                .build();
        UpdateFulfillmentSupplierResult expectedResult = UpdateFulfillmentSupplierResult.of("수정 성공", "200", "SUP001");
        when(updateFulfillmentSupplierPort.updateSupplier(command)).thenReturn(expectedResult);

        // when
        UpdateFulfillmentSupplierResult result = service.updateSupplier(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentSupplierPort).updateSupplier(command);
    }
}
