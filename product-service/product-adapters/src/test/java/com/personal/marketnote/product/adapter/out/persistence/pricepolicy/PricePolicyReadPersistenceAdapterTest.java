package com.personal.marketnote.product.adapter.out.persistence.pricepolicy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricePolicyReadPersistenceAdapterTest {

    @Test
    @DisplayName("PricePolicyReadPersistenceAdapter 빈을 생성할 수 있다")
    void canInstantiateBean() {
        PricePolicyReadPersistenceAdapter adapter = new PricePolicyReadPersistenceAdapter();

        assertThat(adapter).isNotNull();
    }
}
