package com.personal.marketnote.product.adapter.in.web.product.mapper;

import com.personal.marketnote.product.adapter.in.web.cart.request.GetMyOrderingProductsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.OrderingItemRequest;
import com.personal.marketnote.product.adapter.in.web.category.request.RegisterProductCategoriesRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.RegisterProductOptionRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.ReorderProductTagsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductRequest;
import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.command.RegisterProductCategoriesCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductOptionsCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestToCommandMapperTest {

    @Nested
    @DisplayName("RegisterProductRequest → RegisterProductCommand")
    class RegisterProductRequestMap {

        @Test
        @DisplayName("필드를 그대로 매핑하고 fulfillmentVendorGoods가 null이면 null로 매핑한다")
        void mapsFieldsIncludingNullFulfillment() {
            RegisterProductRequest request = new RegisterProductRequest(
                    10L,
                    "상품명",
                    "브랜드",
                    "상세",
                    10_000L,
                    8_000L,
                    100L,
                    true,
                    List.of("tag1", "tag2"),
                    null
            );

            RegisterProductCommand command = ProductRequestToCommandMapper.mapToCommand(request);

            assertThat(command.sellerId()).isEqualTo(10L);
            assertThat(command.name()).isEqualTo("상품명");
            assertThat(command.brandName()).isEqualTo("브랜드");
            assertThat(command.detail()).isEqualTo("상세");
            assertThat(command.price()).isEqualTo(10_000L);
            assertThat(command.discountPrice()).isEqualTo(8_000L);
            assertThat(command.accumulatedPoint()).isEqualTo(100L);
            assertThat(command.isFindAllOptions()).isTrue();
            assertThat(command.tags()).containsExactly("tag1", "tag2");
            assertThat(command.fulfillmentVendorGoods()).isNull();
        }
    }

    @Nested
    @DisplayName("RegisterProductCategoriesRequest → RegisterProductCategoriesCommand")
    class RegisterProductCategoriesRequestMap {

        @Test
        @DisplayName("productId와 categoryIds를 매핑한다")
        void mapsProductIdAndCategoryIds() {
            RegisterProductCategoriesRequest request = new RegisterProductCategoriesRequest();
            ReflectionTestUtils.setField(request, "categoryIds", List.of(1L, 2L));

            RegisterProductCategoriesCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.categoryIds()).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("UpdateProductOptionsRequest → RegisterProductOptionsCommand")
    class RegisterProductOptionsCommandMap {

        @Test
        @DisplayName("options를 OptionItem 리스트로 매핑한다")
        void mapsOptionsToOptionItems() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            RegisterProductOptionRequest option = new RegisterProductOptionRequest();
            ReflectionTestUtils.setField(option, "content", "레드");
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.of(option));

            RegisterProductOptionsCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).hasSize(1);
            assertThat(command.options().getFirst().content()).isEqualTo("레드");
        }
    }

    @Nested
    @DisplayName("UpdateProductOptionsRequest → UpdateProductOptionsCommand")
    class UpdateProductOptionsCommandMap {

        @Test
        @DisplayName("productId/optionCategoryId/categoryName/options를 매핑한다")
        void mapsUpdateCommand() {
            UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
            RegisterProductOptionRequest option = new RegisterProductOptionRequest();
            ReflectionTestUtils.setField(option, "content", "블루");
            ReflectionTestUtils.setField(request, "categoryName", "색상");
            ReflectionTestUtils.setField(request, "options", List.of(option));

            UpdateProductOptionsCommand command = ProductRequestToCommandMapper.mapToUpdateCommand(100L, 7L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.optionCategoryId()).isEqualTo(7L);
            assertThat(command.categoryName()).isEqualTo("색상");
            assertThat(command.options()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("UpdateProductRequest → UpdateProductCommand")
    class UpdateProductCommandMap {

        @Test
        @DisplayName("id와 요청 필드를 매핑한다")
        void mapsUpdateProductCommand() {
            UpdateProductRequest request = new UpdateProductRequest(
                    "상품명",
                    "브랜드",
                    "상세",
                    false,
                    List.of("tag1"),
                    null
            );

            UpdateProductCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.id()).isEqualTo(100L);
            assertThat(command.name()).isEqualTo("상품명");
            assertThat(command.brandName()).isEqualTo("브랜드");
            assertThat(command.detail()).isEqualTo("상세");
            assertThat(command.isFindAllOptions()).isFalse();
            assertThat(command.tags()).containsExactly("tag1");
            assertThat(command.fulfillmentVendorGoods()).isNull();
        }
    }

    @Nested
    @DisplayName("ReorderProductTagsRequest → ReorderProductTagsCommand")
    class ReorderCommandMap {

        @Test
        @DisplayName("productId와 tagOrders를 매핑한다")
        void mapsReorderCommand() {
            ReorderProductTagsRequest request = new ReorderProductTagsRequest(
                    List.of(new ReorderProductTagsRequest.TagOrderItem(1L, 1L),
                            new ReorderProductTagsRequest.TagOrderItem(2L, 2L))
            );

            ReorderProductTagsCommand command = ProductRequestToCommandMapper.mapToCommand(100L, request);

            assertThat(command.productId()).isEqualTo(100L);
            assertThat(command.tagOrders()).hasSize(2);
            assertThat(command.tagOrders().get(0).tagId()).isEqualTo(1L);
            assertThat(command.tagOrders().get(0).orderNum()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("GetMyOrderingProductsRequest → GetMyOrderingProductsQuery")
    class GetMyOrderingProductsMap {

        @Test
        @DisplayName("orderingItemRequests를 OrderingItemQuery 리스트로 매핑한다")
        void mapsOrderingItems() {
            UUID sharerKey = UUID.randomUUID();
            GetMyOrderingProductsRequest request = new GetMyOrderingProductsRequest(
                    List.of(new OrderingItemRequest(
                            500L, sharerKey, (short) 2, "https://cdn.example.com/img.png"
                    ))
            );

            GetMyOrderingProductsQuery query = ProductRequestToCommandMapper.mapToCommand(request);

            assertThat(query.orderingItemQueries()).hasSize(1);
            assertThat(query.orderingItemQueries().getFirst().pricePolicyId()).isEqualTo(500L);
            assertThat(query.orderingItemQueries().getFirst().sharerKey()).isEqualTo(sharerKey);
            assertThat(query.orderingItemQueries().getFirst().quantity()).isEqualTo((short) 2);
            assertThat(query.orderingItemQueries().getFirst().imageUrl()).isEqualTo("https://cdn.example.com/img.png");
        }
    }
}
