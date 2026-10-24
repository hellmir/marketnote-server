package com.personal.marketnote.fulfillment.adapter.out.vendor.fassto.goods;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentGoodsMapper 테스트")
class FulfillmentGoodsMapperTest {

    @Mock
    private FulfillmentGoodsItemMapper firstItem;

    @Mock
    private FulfillmentGoodsItemMapper secondItem;

    @Nested
    @DisplayName("getProductKey()")
    class GetProductKeyTest {

        @Test
        @DisplayName("goods의 첫 번째 cstGodCd(productKey UUID 문자열)를 반환한다")
        void returnsFirstGoodsProductKey() {
            // given
            UUID productKey = UUID.randomUUID();
            lenient().when(firstItem.getCstGodCd()).thenReturn(productKey.toString());
            FulfillmentGoodsMapper mapper = FulfillmentGoodsMapper.register(
                    "CUST001", "token", List.of(firstItem)
            );

            // when
            String result = mapper.getProductKey();

            // then
            assertThat(result).isEqualTo(productKey.toString());
        }

        @Test
        @DisplayName("goods가 여러 개여도 첫 번째 cstGodCd만 반환한다")
        void returnsOnlyFirstWhenMultipleGoods() {
            // given
            UUID firstKey = UUID.randomUUID();
            UUID secondKey = UUID.randomUUID();
            lenient().when(firstItem.getCstGodCd()).thenReturn(firstKey.toString());
            lenient().when(secondItem.getCstGodCd()).thenReturn(secondKey.toString());
            FulfillmentGoodsMapper mapper = FulfillmentGoodsMapper.register(
                    "CUST001", "token", List.of(firstItem, secondItem)
            );

            // when
            String result = mapper.getProductKey();

            // then
            assertThat(result).isEqualTo(firstKey.toString());
            assertThat(result).isNotEqualTo(secondKey.toString());
        }
    }

    @Nested
    @DisplayName("register() 유효성 검증")
    class RegisterValidationTest {

        @Test
        @DisplayName("customerCode가 null이면 FulfillmentQueryParameterNoValueException을 던진다")
        void throwsWhenCustomerCodeIsNull() {
            assertThatThrownBy(() -> FulfillmentGoodsMapper.register(null, "token", List.of(firstItem)))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("accessToken이 null이면 FulfillmentQueryParameterNoValueException을 던진다")
        void throwsWhenAccessTokenIsNull() {
            assertThatThrownBy(() -> FulfillmentGoodsMapper.register("CUST001", null, List.of(firstItem)))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("goods가 비어있으면 FulfillmentQueryParameterNoValueException을 던진다")
        void throwsWhenGoodsIsEmpty() {
            assertThatThrownBy(() -> FulfillmentGoodsMapper.register("CUST001", "token", List.of()))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }
}
