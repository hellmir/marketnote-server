package com.personal.marketnote.product.adapter.in.web.product.response;

import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterProductResponseTest {

    @Test
    @DisplayName("RegisterProductResult로부터 id와 productKey가 Response에 매핑된다")
    void shouldMapIdAndProductKeyFromResult() {
        Long id = 10L;
        UUID productKey = UUID.fromString("018f0000-0000-7000-8000-000000000001");
        RegisterProductResult result = new RegisterProductResult(id, productKey);

        RegisterProductResponse response = RegisterProductResponse.from(result);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.productKey()).isEqualTo(productKey);
    }
}
