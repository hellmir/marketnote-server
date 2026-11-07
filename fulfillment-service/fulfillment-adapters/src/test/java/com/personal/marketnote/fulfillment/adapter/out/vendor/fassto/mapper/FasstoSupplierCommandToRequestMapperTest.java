package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.supplier.FulfillmentSupplierMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.supplier.FulfillmentSupplierQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoSupplierCommandToRequestMapper 테스트")
class FasstoSupplierCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToRegisterRequest - 공급사 등록")
    class MapToRegisterRequest {

        @Test
        @DisplayName("공급사 필드가 FulfillmentSupplierMapper로 매핑되고 supCd는 'registration'으로 고정된다")
        void shouldMapAllFields() {
            // given
            RegisterFulfillmentSupplierCommand command = RegisterFulfillmentSupplierCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("token")
                    .supplierName("공급사명")
                    .customerSupplierCode("CSTSUP001")
                    .useYn("Y")
                    .dealStartDate("20260101")
                    .dealEndDate("20261231")
                    .zipCode("06000")
                    .address1("서울시 강남구")
                    .address2("101호")
                    .ceoName("김대표")
                    .businessNumber("123-45-67890")
                    .businessCategory("업태")
                    .businessType("업종")
                    .phoneNumber("02-1234-5678")
                    .faxNumber("02-1234-5679")
                    .primaryManagerName("담당자1")
                    .primaryManagerPosition("직책1")
                    .primaryManagerPhoneNumber("010-1111-2222")
                    .primaryManagerEmail("m1@example.com")
                    .secondaryManagerName("담당자2")
                    .secondaryManagerPosition("직책2")
                    .secondaryManagerPhoneNumber("010-3333-4444")
                    .secondaryManagerEmail("m2@example.com")
                    .build();

            // when
            FulfillmentSupplierMapper mapper = FasstoSupplierCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.getCustomerCode()).isEqualTo("CUST001");
            assertThat(mapper.getAccessToken()).isEqualTo("token");
            Map<String, Object> payload = mapper.toPayload();
            assertThat(payload)
                    .containsEntry("supCd", "registration")
                    .containsEntry("supNm", "공급사명")
                    .containsEntry("cstSupCd", "CSTSUP001")
                    .containsEntry("useYn", "Y")
                    .containsEntry("dealStrDt", "20260101")
                    .containsEntry("dealEndDt", "20261231")
                    .containsEntry("zipNo", "06000")
                    .containsEntry("addr1", "서울시 강남구")
                    .containsEntry("addr2", "101호")
                    .containsEntry("ceoNm", "김대표")
                    .containsEntry("busNo", "123-45-67890")
                    .containsEntry("busSp", "업태")
                    .containsEntry("busTp", "업종")
                    .containsEntry("telNo", "02-1234-5678")
                    .containsEntry("faxNo", "02-1234-5679")
                    .containsEntry("empNm1", "담당자1")
                    .containsEntry("empPosit1", "직책1")
                    .containsEntry("empTelNo1", "010-1111-2222")
                    .containsEntry("empEmail1", "m1@example.com")
                    .containsEntry("empNm2", "담당자2")
                    .containsEntry("empPosit2", "직책2")
                    .containsEntry("empTelNo2", "010-3333-4444")
                    .containsEntry("empEmail2", "m2@example.com");
        }

        @Test
        @DisplayName("선택 필드가 null이면 payload에서 제외된다")
        void shouldOmitNullOptionalFields() {
            // given
            RegisterFulfillmentSupplierCommand command = RegisterFulfillmentSupplierCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("token")
                    .supplierName("공급사명")
                    .build();

            // when
            FulfillmentSupplierMapper mapper = FasstoSupplierCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload();
            assertThat(payload).containsOnlyKeys("supCd", "supNm");
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - 공급사 수정")
    class MapToUpdateRequest {

        @Test
        @DisplayName("supplierCode가 supCd로 매핑된다")
        void shouldMapSupplierCode() {
            // given
            UpdateFulfillmentSupplierCommand command = UpdateFulfillmentSupplierCommand.builder()
                    .customerCode("CUST001")
                    .accessToken("token")
                    .supplierCode("SUP001")
                    .supplierName("공급사명")
                    .customerSupplierCode("CSTSUP001")
                    .useYn("Y")
                    .dealStartDate("20260101")
                    .dealEndDate("20261231")
                    .zipCode("06000")
                    .address1("서울시 강남구")
                    .address2("101호")
                    .ceoName("김대표")
                    .businessNumber("123-45-67890")
                    .businessCategory("업태")
                    .businessType("업종")
                    .phoneNumber("02-1234-5678")
                    .faxNumber("02-1234-5679")
                    .primaryManagerName("담당자1")
                    .primaryManagerPosition("직책1")
                    .primaryManagerPhoneNumber("010-1111-2222")
                    .primaryManagerEmail("m1@example.com")
                    .secondaryManagerName("담당자2")
                    .secondaryManagerPosition("직책2")
                    .secondaryManagerPhoneNumber("010-3333-4444")
                    .secondaryManagerEmail("m2@example.com")
                    .build();

            // when
            FulfillmentSupplierMapper mapper = FasstoSupplierCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            assertThat(mapper.toPayload()).containsEntry("supCd", "SUP001");
        }
    }

    @Nested
    @DisplayName("mapToSuppliersQuery - 공급사 목록 조회")
    class MapToSuppliersQuery {

        @Test
        @DisplayName("customerCode와 accessToken이 FulfillmentSupplierQuery로 매핑된다")
        void shouldMapBasicFields() {
            // given
            GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "token");

            // when
            FulfillmentSupplierQuery query = FasstoSupplierCommandToRequestMapper.mapToSuppliersQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
        }
    }
}
