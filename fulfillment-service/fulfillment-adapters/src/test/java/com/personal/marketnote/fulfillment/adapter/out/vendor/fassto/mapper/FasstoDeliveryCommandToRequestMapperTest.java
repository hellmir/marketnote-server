package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.mapper;

import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryCancelMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryCarMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryGoodDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryIcsCompletionMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryIcsMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryMapper;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryOutOrdGoodsByOrdNoQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryOutOrdGoodsDetailQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryQuery;
import com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.delivery.FulfillmentDeliveryStatusQuery;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FasstoDeliveryCommandToRequestMapper 테스트")
class FasstoDeliveryCommandToRequestMapperTest {

    @Nested
    @DisplayName("mapToQuery - 배송 목록 조회")
    class MapToQuery {

        @Test
        @DisplayName("모든 필드가 FulfillmentDeliveryQuery로 매핑된다")
        void shouldMapAllFields() {
            // given
            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "token", "20260101", "20260131", "A1", "O", "ORD001"
            );

            // when
            FulfillmentDeliveryQuery query = FasstoDeliveryCommandToRequestMapper.mapToQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getStartDate()).isEqualTo("20260101");
            assertThat(query.getEndDate()).isEqualTo("20260131");
            assertThat(query.getStatus()).isEqualTo("A1");
            assertThat(query.getOutDiv()).isEqualTo("O");
            assertThat(query.getOrdNo()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("orderNumber가 null이어도 매핑된다")
        void shouldMapWhenOrderNumberIsNull() {
            // given
            GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                    "CUST001", "token", "20260101", "20260131", "A1", "O"
            );

            // when
            FulfillmentDeliveryQuery query = FasstoDeliveryCommandToRequestMapper.mapToQuery(command);

            // then
            assertThat(query.getOrdNo()).isNull();
        }
    }

    @Nested
    @DisplayName("mapToDetailQuery - 배송 상세 조회")
    class MapToDetailQuery {

        @Test
        @DisplayName("모든 필드가 FulfillmentDeliveryDetailQuery로 매핑된다")
        void shouldMapAllFields() {
            // given
            GetFulfillmentDeliveryDetailCommand command = GetFulfillmentDeliveryDetailCommand.of(
                    "CUST001", "token", "SLIP001", "ORD001"
            );

            // when
            FulfillmentDeliveryDetailQuery query = FasstoDeliveryCommandToRequestMapper.mapToDetailQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getSlipNo()).isEqualTo("SLIP001");
            assertThat(query.getOrdNo()).isEqualTo("ORD001");
        }

        @Test
        @DisplayName("orderNumber가 null이어도 매핑된다")
        void shouldMapWhenOrderNumberIsNull() {
            // given
            GetFulfillmentDeliveryDetailCommand command = GetFulfillmentDeliveryDetailCommand.of(
                    "CUST001", "token", "SLIP001"
            );

            // when
            FulfillmentDeliveryDetailQuery query = FasstoDeliveryCommandToRequestMapper.mapToDetailQuery(command);

            // then
            assertThat(query.getOrdNo()).isNull();
        }
    }

    @Nested
    @DisplayName("mapToDeliveryStatusQuery - 배송 상태 조회")
    class MapToDeliveryStatusQuery {

        @Test
        @DisplayName("모든 필드가 FulfillmentDeliveryStatusQuery로 매핑된다")
        void shouldMapAllFields() {
            // given
            GetFulfillmentDeliveryStatusesCommand command = GetFulfillmentDeliveryStatusesCommand.of(
                    "CUST001", "token", "20260101", "20260131", "O"
            );

            // when
            FulfillmentDeliveryStatusQuery query = FasstoDeliveryCommandToRequestMapper.mapToDeliveryStatusQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getStartDate()).isEqualTo("20260101");
            assertThat(query.getEndDate()).isEqualTo("20260131");
            assertThat(query.getOutDiv()).isEqualTo("O");
        }
    }

    @Nested
    @DisplayName("mapToOutOrdGoodsDetailQuery - 출고 상품 상세")
    class MapToOutOrdGoodsDetailQuery {

        @Test
        @DisplayName("releaseOrderSlipNumber가 outOrdSlipNo로 매핑된다")
        void shouldMapReleaseOrderSlipNumber() {
            // given
            GetFulfillmentDeliveryOutOrdGoodsDetailCommand command =
                    GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of("CUST001", "token", "REL001");

            // when
            FulfillmentDeliveryOutOrdGoodsDetailQuery query =
                    FasstoDeliveryCommandToRequestMapper.mapToOutOrdGoodsDetailQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getOutOrdSlipNo()).isEqualTo("REL001");
        }
    }

    @Nested
    @DisplayName("mapToOutOrdGoodsByOrdNoQuery - 주문번호로 출고 조회")
    class MapToOutOrdGoodsByOrdNoQuery {

        @Test
        @DisplayName("모든 필드가 매핑된다")
        void shouldMapAllFields() {
            // given
            GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command =
                    GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.of(
                            "CUST001", "token", "20260101", "20260131", "ORD001"
                    );

            // when
            FulfillmentDeliveryOutOrdGoodsByOrdNoQuery query =
                    FasstoDeliveryCommandToRequestMapper.mapToOutOrdGoodsByOrdNoQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getStartDate()).isEqualTo("20260101");
            assertThat(query.getEndDate()).isEqualTo("20260131");
            assertThat(query.getOrdNo()).isEqualTo("ORD001");
        }
    }

    @Nested
    @DisplayName("mapToDeliveryGoodDetailQuery - 배송 상품 상세")
    class MapToDeliveryGoodDetailQuery {

        @Test
        @DisplayName("모든 필드가 FulfillmentDeliveryGoodDetailQuery로 매핑된다")
        void shouldMapAllFields() {
            // given
            GetFulfillmentDeliveryGoodDetailCommand command = GetFulfillmentDeliveryGoodDetailCommand.of(
                    "CUST001", "token", "20260101", "20260131", "ORD001"
            );

            // when
            FulfillmentDeliveryGoodDetailQuery query =
                    FasstoDeliveryCommandToRequestMapper.mapToDeliveryGoodDetailQuery(command);

            // then
            assertThat(query.getCustomerCode()).isEqualTo("CUST001");
            assertThat(query.getAccessToken()).isEqualTo("token");
            assertThat(query.getStartDate()).isEqualTo("20260101");
            assertThat(query.getEndDate()).isEqualTo("20260131");
            assertThat(query.getOrdNo()).isEqualTo("ORD001");
        }
    }

    @Nested
    @DisplayName("mapToCancelRequest - 배송 취소 요청")
    class MapToCancelRequest {

        @Test
        @DisplayName("취소 아이템 목록이 FulfillmentDeliveryCancelMapper로 매핑된다")
        void shouldMapCancelItems() {
            // given
            CancelFulfillmentDeliveryItemCommand item =
                    CancelFulfillmentDeliveryItemCommand.of("SLIP001", "ORD001", "GOD001");
            CancelFulfillmentDeliveryCommand command =
                    CancelFulfillmentDeliveryCommand.of("CUST001", "token", List.of(item));

            // when
            FulfillmentDeliveryCancelMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToCancelRequest(command);

            // then
            assertThat(mapper.getCustomerCode()).isEqualTo("CUST001");
            assertThat(mapper.getAccessToken()).isEqualTo("token");
            assertThat(mapper.toPayload()).hasSize(1);
            assertThat(mapper.toPayload().getFirst())
                    .containsEntry("slipNo", "SLIP001")
                    .containsEntry("ordNo", "ORD001")
                    .containsEntry("cstGodCd", "GOD001");
        }

        @Test
        @DisplayName("productKey가 없으면 cstGodCd가 payload에서 제외된다")
        void shouldOmitCstGodCdWhenProductKeyAbsent() {
            // given
            CancelFulfillmentDeliveryItemCommand item =
                    CancelFulfillmentDeliveryItemCommand.of("SLIP001", "ORD001");
            CancelFulfillmentDeliveryCommand command =
                    CancelFulfillmentDeliveryCommand.of("CUST001", "token", List.of(item));

            // when
            FulfillmentDeliveryCancelMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToCancelRequest(command);

            // then
            assertThat(mapper.toPayload().getFirst()).doesNotContainKey("cstGodCd");
        }
    }

    @Nested
    @DisplayName("mapToRegisterRequest - 배송 등록 요청 (145L 필드)")
    class MapToRegisterRequest {

        @Test
        @DisplayName("주문/수취인/상품/옵션 필드가 모두 매핑된다")
        void shouldMapAllFields() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    productKey, "GOD001", "20261231", 3
            );
            RegisterFulfillmentDeliveryItemCommand item = RegisterFulfillmentDeliveryItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .orderSequence(1)
                    .slipNumber("SLIP001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .releaseMethod("NORMAL")
                    .senderName("판매자")
                    .senderPhoneNumber("010-9876-5432")
                    .salesChannel("CH01")
                    .shippingRequest("문앞")
                    .products(List.of(goods))
                    .sameDayDeliveryCode("SAMEDAY")
                    .remark("비고")
                    .build();
            RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.getCustomerCode()).isEqualTo("CUST001");
            assertThat(mapper.getAccessToken()).isEqualTo("token");
            assertThat(mapper.getOrdNo()).isEqualTo("ORD001");
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload)
                    .containsEntry("ordDt", "20260101")
                    .containsEntry("ordNo", "ORD001")
                    .containsEntry("ordSeq", 1)
                    .containsEntry("slipNo", "SLIP001")
                    .containsEntry("custNm", "홍길동")
                    .containsEntry("custTelNo", "010-1234-5678")
                    .containsEntry("custAddr", "서울시 강남구")
                    .containsEntry("outWay", "NORMAL")
                    .containsEntry("sendNm", "판매자")
                    .containsEntry("sendTelNo", "010-9876-5432")
                    .containsEntry("salChanel", "CH01")
                    .containsEntry("shipReqTerm", "문앞")
                    .containsEntry("oneDayDeliveryCd", "SAMEDAY")
                    .containsEntry("remark", "비고");
        }

        @Test
        @DisplayName("productKey가 있으면 UUID가 cstGodCd로 매핑되고 productCode는 제외된다")
        void shouldPreferProductKeyOverProductCode() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    productKey, "GOD001", "20261231", 3
            );
            RegisterFulfillmentDeliveryItemCommand item = RegisterFulfillmentDeliveryItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .releaseMethod("NORMAL")
                    .products(List.of(goods))
                    .build();
            RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            String payload = mapper.toPayload().toString();
            assertThat(payload).contains(productKey.toString());
            assertThat(payload).doesNotContain("GOD001");
        }

        @Test
        @DisplayName("productKey가 null이면 productCode가 cstGodCd로 폴백된다")
        void shouldFallbackToProductCodeWhenProductKeyAbsent() {
            // given
            RegisterFulfillmentDeliveryGoodsCommand goods =
                    RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", 3);
            RegisterFulfillmentDeliveryItemCommand item = RegisterFulfillmentDeliveryItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .releaseMethod("NORMAL")
                    .products(List.of(goods))
                    .build();
            RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            assertThat(mapper.toPayload().toString()).contains("GOD001");
        }

        @Test
        @DisplayName("선택 필드가 null이면 payload에서 제외된다")
        void shouldOmitNullOptionalFields() {
            // given
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", 3);
            RegisterFulfillmentDeliveryItemCommand item = RegisterFulfillmentDeliveryItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .releaseMethod("NORMAL")
                    .products(List.of(goods))
                    .build();
            RegisterFulfillmentDeliveryCommand command = RegisterFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload).doesNotContainKeys(
                    "ordSeq", "slipNo", "sendNm", "sendTelNo",
                    "salChanel", "shipReqTerm", "oneDayDeliveryCd", "remark"
            );
        }
    }

    @Nested
    @DisplayName("mapToUpdateRequest - 배송 수정 요청")
    class MapToUpdateRequest {

        @Test
        @DisplayName("수정 요청에는 slipNumber가 필수로 매핑된다")
        void shouldMapWithSlipNumber() {
            // given
            UUID productKey = UUID.randomUUID();
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of(
                    productKey, 3
            );
            UpdateFulfillmentDeliveryItemCommand item = UpdateFulfillmentDeliveryItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .slipNumber("SLIP001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .releaseMethod("NORMAL")
                    .products(List.of(goods))
                    .build();
            UpdateFulfillmentDeliveryCommand command = UpdateFulfillmentDeliveryCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToUpdateRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload).containsEntry("slipNo", "SLIP001");
        }
    }

    @Nested
    @DisplayName("mapToRegisterCarRequest - 차량 배송 등록")
    class MapToRegisterCarRequest {

        @Test
        @DisplayName("차량 배송 필드가 FulfillmentDeliveryCarMapper로 매핑된다")
        void shouldMapCarFields() {
            // given
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", 3);
            RegisterFulfillmentDeliveryCarItemCommand item = RegisterFulfillmentDeliveryCarItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .slipNumber("SLIP001")
                    .releaseMethod("CAR")
                    .shopCode("SHOP001")
                    .products(List.of(goods))
                    .remark("차량 배송")
                    .build();
            RegisterFulfillmentDeliveryCarCommand command = RegisterFulfillmentDeliveryCarCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryCarMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterCarRequest(command);

            // then
            assertThat(mapper.getOrdNo()).isEqualTo("ORD001");
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload)
                    .containsEntry("ordDt", "20260101")
                    .containsEntry("ordNo", "ORD001")
                    .containsEntry("slipNo", "SLIP001")
                    .containsEntry("outWay", "CAR")
                    .containsEntry("cstShopCd", "SHOP001")
                    .containsEntry("remark", "차량 배송");
        }
    }

    @Nested
    @DisplayName("mapToUpdateCarRequest - 차량 배송 수정")
    class MapToUpdateCarRequest {

        @Test
        @DisplayName("차량 배송 수정 필드가 매핑된다")
        void shouldMapCarUpdateFields() {
            // given
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", 3);
            UpdateFulfillmentDeliveryCarItemCommand item = UpdateFulfillmentDeliveryCarItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .slipNumber("SLIP001")
                    .releaseMethod("CAR")
                    .shopCode("SHOP001")
                    .products(List.of(goods))
                    .remark("수정")
                    .build();
            UpdateFulfillmentDeliveryCarCommand command = UpdateFulfillmentDeliveryCarCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryCarMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToUpdateCarRequest(command);

            // then
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload).containsEntry("slipNo", "SLIP001");
        }
    }

    @Nested
    @DisplayName("mapToRegisterIcsRequest - ICS 배송 등록")
    class MapToRegisterIcsRequest {

        @Test
        @DisplayName("ICS 필드가 FulfillmentDeliveryIcsMapper로 매핑된다")
        void shouldMapIcsFields() {
            // given
            RegisterFulfillmentDeliveryGoodsCommand goods = RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", 3);
            RegisterFulfillmentDeliveryIcsItemCommand item = RegisterFulfillmentDeliveryIcsItemCommand.builder()
                    .orderDate("20260101")
                    .orderNumber("ORD001")
                    .platform("ICS_PLATFORM")
                    .logisticsCenter("LC01")
                    .invoiceNumber("INV001")
                    .recipientName("홍길동")
                    .recipientPhoneNumber("010-1234-5678")
                    .recipientAddress("서울시 강남구")
                    .senderName("판매자")
                    .senderPhoneNumber("010-9876-5432")
                    .salesChannel("CH01")
                    .shippingRequest("문앞")
                    .remark("비고")
                    .products(List.of(goods))
                    .build();
            RegisterFulfillmentDeliveryIcsCommand command = RegisterFulfillmentDeliveryIcsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryIcsMapper mapper = FasstoDeliveryCommandToRequestMapper.mapToRegisterIcsRequest(command);

            // then
            assertThat(mapper.getOrdNo()).isEqualTo("ORD001");
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload)
                    .containsEntry("ordDt", "20260101")
                    .containsEntry("ordNo", "ORD001")
                    .containsEntry("platform", "ICS_PLATFORM")
                    .containsEntry("logiCenter", "LC01")
                    .containsEntry("invoiceNo", "INV001")
                    .containsEntry("custNm", "홍길동")
                    .containsEntry("sendNm", "판매자");
        }
    }

    @Nested
    @DisplayName("mapToIcsCompletionRequest - ICS 완료 요청")
    class MapToIcsCompletionRequest {

        @Test
        @DisplayName("주문번호 리스트가 FulfillmentDeliveryIcsCompletionMapper로 매핑된다")
        void shouldMapOrderNumbers() {
            // given
            CompleteFulfillmentDeliveryIcsItemCommand item = CompleteFulfillmentDeliveryIcsItemCommand.of(
                    List.of("ORD001", "ORD002")
            );
            CompleteFulfillmentDeliveryIcsCommand command = CompleteFulfillmentDeliveryIcsCommand.of(
                    "CUST001", "token", List.of(item)
            );

            // when
            FulfillmentDeliveryIcsCompletionMapper mapper =
                    FasstoDeliveryCommandToRequestMapper.mapToIcsCompletionRequest(command);

            // then
            assertThat(mapper.getCustomerCode()).isEqualTo("CUST001");
            assertThat(mapper.getAccessToken()).isEqualTo("token");
            Map<String, Object> payload = mapper.toPayload().getFirst();
            assertThat(payload).containsEntry("ordNoList", List.of("ORD001", "ORD002"));
        }
    }
}
