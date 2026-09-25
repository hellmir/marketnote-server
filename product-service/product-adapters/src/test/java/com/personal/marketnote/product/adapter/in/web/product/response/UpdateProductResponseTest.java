package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.UpdateProductResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductResponseTest {

    @Test
    @DisplayName("UpdateProductResult로부터 id와 productKey가 Response에 매핑된다")
    void shouldMapIdAndProductKeyFromResult() {
        Long id = 10L;
        UUID productKey = UUID.fromString("018f0000-0000-7000-8000-000000000001");
        UpdateProductResult result = new UpdateProductResult(id, productKey);

        UpdateProductResponse response = UpdateProductResponse.from(result);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.productKey()).isEqualTo(productKey);
    }
}
