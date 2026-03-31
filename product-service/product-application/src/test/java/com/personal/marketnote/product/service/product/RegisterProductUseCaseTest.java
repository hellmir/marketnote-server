package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.product.SaveProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterProductUseCaseTest {
    @Mock
    private RegisterPricePolicyUseCase registerPricePolicyUseCase;
    @Mock
    private SaveProductPort saveProductPort;
    @Mock
    private PublishProductEventPort publishProductEventPort;

    @InjectMocks
    private RegisterProductService registerProductService;

    @Test
    @DisplayName("상품 등록 시 기본 풀필먼트 정보로 등록하고 결과를 반환한다")
    void registerProduct_success_withDefaultFulfillmentOptions() {
        RegisterProductCommand command = buildCommand(null);
        Product savedProduct = buildSavedProduct(10L, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenReturn(RegisterPricePolicyResult.of(100L));

        RegisterProductResult result = registerProductService.registerProduct(command);

        assertThat(result.id()).isEqualTo(savedProduct.getId());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(saveProductPort).save(productCaptor.capture());
        Product requestProduct = productCaptor.getValue();
        assertThat(requestProduct.getSellerId()).isEqualTo(command.sellerId());
        assertThat(requestProduct.getName()).isEqualTo(command.name());
        assertThat(requestProduct.getBrandName()).isEqualTo(command.brandName());
        assertThat(requestProduct.getDetail()).isEqualTo(command.detail());
        assertThat(requestProduct.isFindAllOptionsYn()).isEqualTo(command.isFindAllOptions());
        assertThat(requestProduct.getProductTags())
                .extracting(ProductTag::getName)
                .containsExactly("tag-1", "tag-2");
        assertThat(requestProduct.getProductTags())
                .extracting(ProductTag::getStatus)
                .containsOnly(EntityStatus.ACTIVE);

        ArgumentCaptor<RegisterPricePolicyCommand> pricePolicyCaptor =
                ArgumentCaptor.forClass(RegisterPricePolicyCommand.class);
        verify(registerPricePolicyUseCase).registerPricePolicy(
                eq(command.sellerId()), eq(false), pricePolicyCaptor.capture()
        );
        RegisterPricePolicyCommand pricePolicyCommand = pricePolicyCaptor.getValue();
        assertThat(pricePolicyCommand.productId()).isEqualTo(savedProduct.getId());
        assertThat(pricePolicyCommand.price()).isEqualTo(command.price());
        assertThat(pricePolicyCommand.discountPrice()).isEqualTo(command.discountPrice());
        assertThat(pricePolicyCommand.accumulatedPoint()).isEqualTo(command.accumulatedPoint());
        assertThat(pricePolicyCommand.optionIds()).isNull();

        verify(publishProductEventPort).publishProductRegisteredEvent(10L, 100L, command.sellerId(), "테스트 상품", null);

        verifyNoMoreInteractions(
                registerPricePolicyUseCase,
                saveProductPort,
                publishProductEventPort
        );
    }

    @Test
    @DisplayName("상품 등록 시 풀필먼트 옵션이 있으면 해당 값으로 등록한다")
    void registerProduct_success_withFulfillmentOptions() {
        FulfillmentVendorGoodsOptionCommand options = buildFulfillmentOptions();
        RegisterProductCommand command = buildCommand(options);
        Product savedProduct = buildSavedProduct(11L, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenReturn(RegisterPricePolicyResult.of(101L));

        registerProductService.registerProduct(command);

        verify(publishProductEventPort).publishProductRegisteredEvent(11L, 101L, command.sellerId(), "테스트 상품", "2");
    }

    @Test
    @DisplayName("상품 등록 시 가격 정책 등록이 실패하면 이후 작업을 수행하지 않는다")
    void registerProduct_registerPricePolicyFails() {
        RegisterProductCommand command = buildCommand(null);
        Product savedProduct = buildSavedProduct(20L, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenThrow(new IllegalStateException("price policy fail"));

        assertThatThrownBy(() -> registerProductService.registerProduct(command))
                .isInstanceOf(IllegalStateException.class);

        verify(saveProductPort).save(any(Product.class));
        verify(registerPricePolicyUseCase).registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        );
        verifyNoInteractions(publishProductEventPort);
    }

    private RegisterProductCommand buildCommand(FulfillmentVendorGoodsOptionCommand fulfillmentVendorGoods) {
        return RegisterProductCommand.builder()
                .sellerId(1L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상세 설명")
                .price(10000L)
                .discountPrice(8000L)
                .accumulatedPoint(100L)
                .isFindAllOptions(true)
                .tags(List.of("tag-1", "tag-2"))
                .fulfillmentVendorGoods(fulfillmentVendorGoods)
                .build();
    }

    private Product buildSavedProduct(Long id, RegisterProductCommand command) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .sellerId(command.sellerId())
                        .name(command.name())
                        .brandName(command.brandName())
                        .detail(command.detail())
                        .findAllOptionsYn(command.isFindAllOptions())
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private FulfillmentVendorGoodsOptionCommand buildFulfillmentOptions() {
        return FulfillmentVendorGoodsOptionCommand.builder()
                .goodsType("2")
                .build();
    }
}
