package com.personal.marketnote.fulfillment.port.in.command.vendor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterFasstoGoodsItemCommandTest {

    @Test
    @DisplayName("builder로 필수 4개 필드만 설정하면 나머지는 null로 생성된다")
    void builder_setsRequiredFieldsAndNullsRest() {
        // given
        String cstGodCd = "123";
        String godNm = "테스트 상품";
        String godType = "1";
        String giftDiv = "01";

        // when
        RegisterFasstoGoodsItemCommand command = RegisterFasstoGoodsItemCommand.builder()
                .cstGodCd(cstGodCd)
                .godNm(godNm)
                .godType(godType)
                .giftDiv(giftDiv)
                .build();

        // then
        assertThat(command.cstGodCd()).isEqualTo("123");
        assertThat(command.godNm()).isEqualTo("테스트 상품");
        assertThat(command.godType()).isEqualTo("1");
        assertThat(command.giftDiv()).isEqualTo("01");

        assertThat(command.godOptCd1()).isNull();
        assertThat(command.godOptCd2()).isNull();
        assertThat(command.invGodNmUseYn()).isNull();
        assertThat(command.invGodNm()).isNull();
        assertThat(command.supCd()).isNull();
        assertThat(command.cateCd()).isNull();
        assertThat(command.seasonCd()).isNull();
        assertThat(command.genderCd()).isNull();
        assertThat(command.makeYr()).isNull();
        assertThat(command.godPr()).isNull();
        assertThat(command.inPr()).isNull();
        assertThat(command.salPr()).isNull();
        assertThat(command.dealTemp()).isNull();
        assertThat(command.pickFac()).isNull();
        assertThat(command.godBarcd()).isNull();
        assertThat(command.boxWeight()).isNull();
        assertThat(command.origin()).isNull();
        assertThat(command.distTermMgtYn()).isNull();
        assertThat(command.useTermDay()).isNull();
        assertThat(command.outCanDay()).isNull();
        assertThat(command.inCanDay()).isNull();
        assertThat(command.boxDiv()).isNull();
        assertThat(command.bufGodYn()).isNull();
        assertThat(command.loadingDirection()).isNull();
        assertThat(command.subMate()).isNull();
        assertThat(command.useYn()).isNull();
        assertThat(command.safetyStock()).isNull();
        assertThat(command.feeYn()).isNull();
        assertThat(command.saleUnitQty()).isNull();
        assertThat(command.cstGodImgUrl()).isNull();
        assertThat(command.externalGodImgUrl()).isNull();
    }
}
