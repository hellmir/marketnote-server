package com.personal.marketnote.fulfillment.port.in.command.vendor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterFulfillmentGoodsItemCommandTest {

    @Test
    @DisplayName("builder로 필수 4개 필드만 설정하면 나머지는 null로 생성된다")
    void builder_setsRequiredFieldsAndNullsRest() {
        // given
        String productCode = "123";
        String productName = "테스트 상품";
        String productType = "1";
        String giftDivision = "01";

        // when
        RegisterFulfillmentGoodsItemCommand command = RegisterFulfillmentGoodsItemCommand.builder()
                .productCode(productCode)
                .productName(productName)
                .productType(productType)
                .giftDivision(giftDivision)
                .build();

        // then
        assertThat(command.productCode()).isEqualTo("123");
        assertThat(command.productName()).isEqualTo("테스트 상품");
        assertThat(command.productType()).isEqualTo("1");
        assertThat(command.giftDivision()).isEqualTo("01");

        assertThat(command.productOptionCode1()).isNull();
        assertThat(command.productOptionCode2()).isNull();
        assertThat(command.inventoryProductNameUseYn()).isNull();
        assertThat(command.inventoryProductName()).isNull();
        assertThat(command.supplierCode()).isNull();
        assertThat(command.categoryCode()).isNull();
        assertThat(command.seasonCode()).isNull();
        assertThat(command.genderCode()).isNull();
        assertThat(command.manufactureYear()).isNull();
        assertThat(command.productPrice()).isNull();
        assertThat(command.inboundPrice()).isNull();
        assertThat(command.salePrice()).isNull();
        assertThat(command.dealTemperature()).isNull();
        assertThat(command.pickFactor()).isNull();
        assertThat(command.productBarcode()).isNull();
        assertThat(command.boxWeight()).isNull();
        assertThat(command.origin()).isNull();
        assertThat(command.expirationManagementYn()).isNull();
        assertThat(command.shelfLifeDays()).isNull();
        assertThat(command.outboundCancelDays()).isNull();
        assertThat(command.inboundCancelDays()).isNull();
        assertThat(command.boxDivision()).isNull();
        assertThat(command.bufferProductYn()).isNull();
        assertThat(command.loadingDirection()).isNull();
        assertThat(command.subMaterial()).isNull();
        assertThat(command.useYn()).isNull();
        assertThat(command.safetyStock()).isNull();
        assertThat(command.feeYn()).isNull();
        assertThat(command.saleUnitQuantity()).isNull();
        assertThat(command.customerProductImageUrl()).isNull();
        assertThat(command.externalProductImageUrl()).isNull();
    }
}
