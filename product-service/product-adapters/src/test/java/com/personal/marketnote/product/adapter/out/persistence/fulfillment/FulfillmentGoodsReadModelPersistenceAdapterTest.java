package com.personal.marketnote.product.adapter.out.persistence.fulfillment;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.product.adapter.out.persistence.fulfillment.entity.FulfillmentGoodsReadModelJpaEntity;
import com.personal.marketnote.product.adapter.out.persistence.fulfillment.repository.FulfillmentGoodsReadModelJpaRepository;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsElementsResult;
import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, FulfillmentGoodsReadModelPersistenceAdapter.class})
class FulfillmentGoodsReadModelPersistenceAdapterTest {

    @Autowired
    private FulfillmentGoodsReadModelPersistenceAdapter adapter;

    @Autowired
    private FulfillmentGoodsReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private FulfillmentGoodsSyncedEvent createEvent(String customerGoodsCode, String goodsName) {
        return new FulfillmentGoodsSyncedEvent(
                customerGoodsCode, "GD001", goodsName, "SINGLE", "단품",
                "Y", null, null, "CUST001", "테스트 고객",
                "SUP001", "테스트 공급사", null, null, null,
                null, "10000", "8000", "12000", null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, "8801234567890", null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, "Y", null, null, "Y",
                null, List.of()
        );
    }

    private FulfillmentGoodsSyncedEvent createEventWithElements(String customerGoodsCode) {
        return new FulfillmentGoodsSyncedEvent(
                customerGoodsCode, "GD001", "테스트 상품", "SET", "세트",
                "Y", null, null, "CUST001", "테스트 고객",
                null, null, null, null, null,
                null, "10000", "8000", "12000", null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, "Y", null, null, "Y",
                null, List.of(
                        new FulfillmentGoodsSyncedEvent.GoodsElementItem(
                                "ELEM001", customerGoodsCode, "8801111111111",
                                "구성품A", "SINGLE", "단품", 2
                        ),
                        new FulfillmentGoodsSyncedEvent.GoodsElementItem(
                                "ELEM002", customerGoodsCode, "8802222222222",
                                "구성품B", "SINGLE", "단품", 1
                        )
                )
        );
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("존재하지 않는 customerGoodsCode로 upsert하면 새 엔티티가 생성된다")
        void insertsNewEntity() {
            // given
            FulfillmentGoodsSyncedEvent event = createEvent("CGC001", "테스트 상품");

            // when
            adapter.upsert(event);

            // then
            assertThat(repository.count()).isEqualTo(1);
            FulfillmentGoodsReadModelJpaEntity saved = repository.findByCustomerGoodsCode("CGC001").orElseThrow();
            assertThat(saved.getGoodsName()).isEqualTo("테스트 상품");
            assertThat(saved.getGoodsCode()).isEqualTo("GD001");
        }

        @Test
        @DisplayName("이미 존재하는 customerGoodsCode로 upsert하면 기존 엔티티가 갱신된다")
        void updatesExistingEntity() {
            // given
            adapter.upsert(createEvent("CGC001", "원래 상품명"));

            // when
            adapter.upsert(createEvent("CGC001", "변경된 상품명"));

            // then
            assertThat(repository.count()).isEqualTo(1);
            assertThat(repository.findByCustomerGoodsCode("CGC001").get().getGoodsName()).isEqualTo("변경된 상품명");
        }
    }

    @Nested
    @DisplayName("getFulfillmentVendorGoods")
    class GetFulfillmentVendorGoods {

        @Test
        @DisplayName("존재하는 customerGoodsCode로 조회하면 상품 정보를 반환한다")
        void returnsGoodsWhenFound() {
            // given
            adapter.upsert(createEvent("CGC001", "테스트 상품"));

            // when
            GetFulfillmentVendorGoodsResult result = adapter.getFulfillmentVendorGoods("CGC001");

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.goods()).hasSize(1);
            assertThat(result.goods().get(0).customerGoodsCode()).isEqualTo("CGC001");
            assertThat(result.goods().get(0).goodsName()).isEqualTo("테스트 상품");
            assertThat(result.goods().get(0).goodsCode()).isEqualTo("GD001");
        }

        @Test
        @DisplayName("존재하지 않는 customerGoodsCode로 조회하면 빈 결과를 반환한다")
        void returnsEmptyWhenNotFound() {
            // when
            GetFulfillmentVendorGoodsResult result = adapter.getFulfillmentVendorGoods("NON_EXISTENT");

            // then
            assertThat(result.dataCount()).isZero();
            assertThat(result.goods()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getFulfillmentVendorGoodsElements")
    class GetFulfillmentVendorGoodsElements {

        @Test
        @DisplayName("모든 활성 상품의 구성품 목록을 반환한다")
        void returnsAllActiveGoodsElements() {
            // given
            adapter.upsert(createEventWithElements("CGC001"));
            adapter.upsert(createEventWithElements("CGC002"));

            // when
            GetFulfillmentVendorGoodsElementsResult result = adapter.getFulfillmentVendorGoodsElements();

            // then
            assertThat(result.dataCount()).isEqualTo(2);
            assertThat(result.elements()).hasSize(2);
            assertThat(result.elements()).anySatisfy(element -> {
                assertThat(element.customerGoodsCode()).isEqualTo("CGC001");
                assertThat(element.elementList()).hasSize(2);
            });
        }
    }
}
