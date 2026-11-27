package com.personal.marketnote.fulfillment.adapter.in.web.vendor.mapper;

import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.RegisterFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.UpdateFulfillmentGoodsRequest;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FulfillmentGoodsRequestToCommandMapper 테스트")
class FulfillmentGoodsRequestToCommandMapperTest {

    @Test
    @DisplayName("mapToRegisterCommand는 cstGodCd를 fasstoProductCode에도 채운다")
    void shouldFillFasstoProductCodeOnRegister() {
        RegisterFulfillmentGoodsRequest request = new RegisterFulfillmentGoodsRequest();
        ReflectionTestUtils.setField(request, "cstGodCd", "GOD001");
        ReflectionTestUtils.setField(request, "godNm", "상품명");

        RegisterFulfillmentGoodsCommand command = FulfillmentGoodsRequestToCommandMapper.mapToRegisterCommand(
            "CUST001", "token", List.of(request)
        );

        assertThat(command.goods()).hasSize(1);
        RegisterFulfillmentGoodsItemCommand item = command.goods().get(0);
        assertThat(item.productCode()).isEqualTo("GOD001");
        assertThat(item.fasstoProductCode()).isEqualTo("GOD001");
    }

    @Test
    @DisplayName("mapToUpdateCommand는 cstGodCd를 fasstoProductCode에도 채운다")
    void shouldFillFasstoProductCodeOnUpdate() {
        UpdateFulfillmentGoodsRequest request = new UpdateFulfillmentGoodsRequest();
        ReflectionTestUtils.setField(request, "cstGodCd", "GOD001");
        ReflectionTestUtils.setField(request, "godNm", "상품명");

        UpdateFulfillmentGoodsCommand command = FulfillmentGoodsRequestToCommandMapper.mapToUpdateCommand(
            "CUST001", "token", List.of(request)
        );

        assertThat(command.goods()).hasSize(1);
        UpdateFulfillmentGoodsItemCommand item = command.goods().get(0);
        assertThat(item.productCode()).isEqualTo("GOD001");
        assertThat(item.fasstoProductCode()).isEqualTo("GOD001");
    }
}
