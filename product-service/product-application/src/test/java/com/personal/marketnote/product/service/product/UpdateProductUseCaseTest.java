package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.kafka.event.ProductUpdatedEvent;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductInfoNoValueException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.product.UpdateProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private UpdateProductPort updateProductPort;
    @Mock
    private PublishProductEventPort publishProductEventPort;

    @InjectMocks
    private UpdateProductService updateProductService;

    @Test
    @DisplayName("상품 수정 시 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void update_notOwner_throws() {
        Long userId = 1L;
        UpdateProductCommand command = buildCommand(10L, null);

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> updateProductService.update(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(getProductUseCase, updateProductPort, publishProductEventPort);
    }

    @Test
    @DisplayName("상품 수정 시 관리자 요청인 경우, 상품 소유 여부와 관계없이 상품을 수정한다")
    void update_adminSkipsOwnerCheck_updatesProduct() {
        UpdateProductCommand command = buildCommand(11L, null);
        Product product = buildProduct(11L, "기존 상품");
        Product refreshed = buildProductWithTags(11L, List.of(
                buildPersistedTag(101L, "tag-1", 1L),
                buildPersistedTag(102L, "tag-2", 2L)
        ));

        when(getProductUseCase.getProduct(11L)).thenReturn(product);
        when(findProductPort.findById(11L)).thenReturn(Optional.of(refreshed));

        updateProductService.update(99L, true, command);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(updateProductPort).update(productCaptor.capture());
        Product updated = productCaptor.getValue();

        assertThat(updated.getName()).isEqualTo(command.name());
        assertThat(updated.getBrandName()).isEqualTo(command.brandName());
        assertThat(updated.getDetail()).isEqualTo(command.detail());
        assertThat(updated.isFindAllOptionsYn()).isEqualTo(command.isFindAllOptions());
        assertThat(updated.getProductTags())
                .extracting(ProductTag::getName)
                .containsExactly("tag-1", "tag-2");
        assertThat(updated.getProductTags())
                .extracting(ProductTag::getStatus)
                .containsOnly(EntityStatus.ACTIVE);
        assertThat(updated.getProductTags())
                .extracting(ProductTag::getProductId)
                .containsOnly(11L);

        verify(getProductUseCase).getProduct(11L);
        verify(findProductPort).findById(11L);
        verify(publishProductEventPort).publishProductTagChangedEvent(
                eq(11L),
                anyList(),
                eq(ProductTagChangedEvent.Action.UPDATED)
        );
        verifyNoMoreInteractions(publishProductEventPort);
    }

    @Test
    @DisplayName("상품 수정 성공 시 풀필먼트 서비스 동기화를 진행한다")
    void update_success_updatesFulfillment() {
        Long userId = 2L;
        FulfillmentVendorGoodsOptionCommand options = buildFulfillmentOptions();
        UpdateProductCommand command = buildCommand(20L, options);
        Product product = buildProduct(20L, "기존 상품");
        Product refreshed = buildProductWithTags(20L, List.of(
                buildPersistedTag(201L, "tag-1", 1L),
                buildPersistedTag(202L, "tag-2", 2L)
        ));

        when(findProductPort.existsByIdAndSellerId(20L, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(20L)).thenReturn(product);
        when(findProductPort.findById(20L)).thenReturn(Optional.of(refreshed));

        updateProductService.update(userId, false, command);

        verify(findProductPort).existsByIdAndSellerId(20L, userId);
        verify(updateProductPort).update(product);

        ArgumentCaptor<ProductUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdatedEvent.class);
        verify(publishProductEventPort).publishProductUpdatedEvent(eventCaptor.capture());

        ProductUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.productId()).isEqualTo(20L);
        assertThat(capturedEvent.productName()).isEqualTo("변경 상품");
        assertThat(capturedEvent.goodsType()).isEqualTo(options.goodsType());
        assertThat(capturedEvent.giftDivision()).isEqualTo(options.giftDivision());

        ArgumentCaptor<List<ProductTagChangedEvent.ProductTagSnapshot>> tagCaptor
                = ArgumentCaptor.forClass(List.class);
        verify(publishProductEventPort).publishProductTagChangedEvent(
                eq(20L), tagCaptor.capture(), eq(ProductTagChangedEvent.Action.UPDATED)
        );
        assertThat(tagCaptor.getValue())
                .extracting(ProductTagChangedEvent.ProductTagSnapshot::id)
                .containsExactly(201L, 202L);
        assertThat(tagCaptor.getValue())
                .extracting(ProductTagChangedEvent.ProductTagSnapshot::name)
                .containsExactly("tag-1", "tag-2");
    }

    @Test
    @DisplayName("상품 수정 후 새로 조회한 상품이 없으면 태그 변경 이벤트를 발행하지 않는다")
    void update_refreshedProductMissing_skipsTagEvent() {
        UpdateProductCommand command = buildCommand(50L, null);
        Product product = buildProduct(50L, "기존 상품");

        when(getProductUseCase.getProduct(50L)).thenReturn(product);
        when(findProductPort.findById(50L)).thenReturn(Optional.empty());

        updateProductService.update(1L, true, command);

        verify(updateProductPort).update(product);
        verify(findProductPort).findById(50L);
        verifyNoInteractions(publishProductEventPort);
    }

    @Test
    @DisplayName("상품 조회에 실패하면 예외를 전파한다")
    void update_getProductFails_propagates() {
        UpdateProductCommand command = buildCommand(30L, null);
        ProductNotFoundException exception = new ProductNotFoundException(30L);

        when(getProductUseCase.getProduct(30L)).thenThrow(exception);

        assertThatThrownBy(() -> updateProductService.update(1L, true, command))
                .isSameAs(exception);

        verify(getProductUseCase).getProduct(30L);
        verifyNoInteractions(findProductPort, updateProductPort, publishProductEventPort);
    }

    @Test
    @DisplayName("상품 업데이트가 실패하면 풀필먼트 서비스 동기화를 진행하지 않는다")
    void update_updateProductFails_skipsFulfillmentUpdate() {
        Long userId = 3L;
        FulfillmentVendorGoodsOptionCommand options = buildFulfillmentOptions();
        UpdateProductCommand command = buildCommand(40L, options);
        Product product = buildProduct(40L, "기존 상품");
        ProductNotFoundException exception = new ProductNotFoundException(40L);

        when(findProductPort.existsByIdAndSellerId(40L, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(40L)).thenReturn(product);
        doThrow(exception).when(updateProductPort).update(product);

        assertThatThrownBy(() -> updateProductService.update(userId, false, command))
                .isSameAs(exception);

        verify(updateProductPort).update(product);
        verifyNoInteractions(publishProductEventPort);
    }

    @Test
    @DisplayName("풀필먼트 서비스 동기화 시 상품 ID가 없으면 예외를 던진다")
    void update_missingProductId_throws() {
        FulfillmentVendorGoodsOptionCommand options = buildFulfillmentOptions();
        UpdateProductCommand command = buildCommand(60L, options);
        Product product = buildProduct(null, "상품명");

        when(getProductUseCase.getProduct(60L)).thenReturn(product);

        assertThatThrownBy(() -> updateProductService.update(1L, true, command))
                .isInstanceOf(ProductInfoNoValueException.class)
                .hasMessageContaining("상품 ID가 존재하지 않습니다");

        verify(updateProductPort).update(product);
        verifyNoInteractions(publishProductEventPort);
    }

    @Test
    @DisplayName("풀필먼트 서비스 동기화 시 상품명이 없으면 예외를 던진다")
    void update_missingProductName_throws() {
        FulfillmentVendorGoodsOptionCommand options = buildFulfillmentOptions();
        UpdateProductCommand command = buildCommandWithName(70L, null, options);
        Product product = buildProduct(70L, null);

        when(getProductUseCase.getProduct(70L)).thenReturn(product);

        assertThatThrownBy(() -> updateProductService.update(1L, true, command))
                .isInstanceOf(ProductInfoNoValueException.class)
                .hasMessageContaining("상품명이 존재하지 않습니다");

        verify(updateProductPort).update(product);
        verifyNoInteractions(publishProductEventPort);
    }

    private UpdateProductCommand buildCommand(Long id, FulfillmentVendorGoodsOptionCommand fulfillmentVendorGoods) {
        return buildCommandWithName(id, "변경 상품", fulfillmentVendorGoods);
    }

    private UpdateProductCommand buildCommandWithName(
            Long id,
            String name,
            FulfillmentVendorGoodsOptionCommand fulfillmentVendorGoods
    ) {
        return UpdateProductCommand.builder()
                .id(id)
                .name(name)
                .brandName("변경 브랜드")
                .detail("변경 상세 설명")
                .isFindAllOptions(true)
                .tags(List.of("tag-1", "tag-2"))
                .fulfillmentVendorGoods(fulfillmentVendorGoods)
                .build();
    }

    private Product buildProduct(Long id, String name) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name(name)
                        .brandName("기존 브랜드")
                        .detail("기존 상세")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private Product buildProductWithTags(Long id, List<ProductTag> tags) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(1L)
                        .name("변경 상품")
                        .brandName("변경 브랜드")
                        .detail("변경 상세 설명")
                        .findAllOptionsYn(true)
                        .productTags(tags)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private ProductTag buildPersistedTag(Long id, String name, Long orderNum) {
        return ProductTag.from(
                ProductTagSnapshotState.builder()
                        .id(id)
                        .productId(11L)
                        .name(name)
                        .orderNum(orderNum)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private FulfillmentVendorGoodsOptionCommand buildFulfillmentOptions() {
        return FulfillmentVendorGoodsOptionCommand.builder()
                .goodsType("2")
                .giftDivision("99")
                .goodsOptionCode1("opt1")
                .goodsOptionCode2("opt2")
                .invoiceGoodsNameEnabled("Y")
                .invoiceGoodsName("상품명")
                .supplierCode("SUP")
                .categoryCode("CATE")
                .seasonCode("2024")
                .genderCode("M")
                .manufactureYear("2024")
                .unitPrice("12000")
                .supplyPrice("9000")
                .salePrice("10000")
                .handlingTemperature("ROOM")
                .pickingFacility("A1")
                .goodsBarcode("BARCODE")
                .boxWeight("2.5")
                .origin("KR")
                .expirationDateManagementEnabled("Y")
                .shelfLifeDays("30")
                .outboundAvailableDays("7")
                .inboundAvailableDays("7")
                .outboundBoxType("BOX")
                .cushioningEnabled("N")
                .loadingDirection("UP")
                .subsidiaryMaterialCode("COTTON")
                .enabled("Y")
                .safetyStock("10")
                .feeApplied("N")
                .saleUnitQuantity("1")
                .customerGoodsImageUrl("https://example.com/image.jpg")
                .externalGoodsImageUrl("https://example.com/external.jpg")
                .build();
    }

}
