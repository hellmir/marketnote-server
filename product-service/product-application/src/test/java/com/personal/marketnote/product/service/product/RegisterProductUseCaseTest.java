package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.exception.ProductInfoNoValueException;
import com.personal.marketnote.product.port.in.command.FulfillmentVendorGoodsOptionCommand;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
import com.personal.marketnote.product.port.out.event.PublishProductEventPort;
import com.personal.marketnote.product.port.out.fulfillment.RegisterFulfillmentVendorGoodsCommand;
import com.personal.marketnote.product.port.out.fulfillment.RegisterFulfillmentVendorGoodsPort;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
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
    @Mock
    private RegisterInventoryPort registerInventoryPort;
    @Mock
    private RegisterFulfillmentVendorGoodsPort registerFulfillmentVendorGoodsPort;

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

        verify(publishProductEventPort).publishProductRegisteredEvent(10L, 100L, command.sellerId());
        verify(registerInventoryPort).registerInventory(10L, 100L);

        ArgumentCaptor<RegisterFulfillmentVendorGoodsCommand> fulfillmentCaptor =
                ArgumentCaptor.forClass(RegisterFulfillmentVendorGoodsCommand.class);
        verify(registerFulfillmentVendorGoodsPort).registerFulfillmentVendorGoods(fulfillmentCaptor.capture());
        RegisterFulfillmentVendorGoodsCommand fulfillmentCommand = fulfillmentCaptor.getValue();
        assertThat(fulfillmentCommand.cstGodCd()).isEqualTo(String.valueOf(savedProduct.getId()));
        assertThat(fulfillmentCommand.godNm()).isEqualTo(savedProduct.getName());
        assertThat(fulfillmentCommand.godType()).isEqualTo("1");
        assertThat(fulfillmentCommand.giftDiv()).isEqualTo("01");
        assertThat(fulfillmentCommand.godOptCd1()).isNull();
        assertThat(fulfillmentCommand.externalGodImgUrl()).isNull();

        verifyNoMoreInteractions(
                registerPricePolicyUseCase,
                saveProductPort,
                publishProductEventPort,
                registerInventoryPort,
                registerFulfillmentVendorGoodsPort
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

        verify(publishProductEventPort).publishProductRegisteredEvent(11L, 101L, command.sellerId());

        ArgumentCaptor<RegisterFulfillmentVendorGoodsCommand> fulfillmentCaptor =
                ArgumentCaptor.forClass(RegisterFulfillmentVendorGoodsCommand.class);
        verify(registerFulfillmentVendorGoodsPort).registerFulfillmentVendorGoods(fulfillmentCaptor.capture());

        RegisterFulfillmentVendorGoodsCommand expected = buildExpectedFulfillmentCommand(savedProduct, options);
        assertThat(fulfillmentCaptor.getValue()).isEqualTo(expected);
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
        verifyNoInteractions(publishProductEventPort, registerInventoryPort, registerFulfillmentVendorGoodsPort);
    }

    @Test
    @DisplayName("상품 등록 시 재고 등록이 실패하면 풀필먼트 등록을 시도하지 않는다")
    void registerProduct_inventoryRegistrationFails() {
        RegisterProductCommand command = buildCommand(null);
        Product savedProduct = buildSavedProduct(30L, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenReturn(RegisterPricePolicyResult.of(200L));
        doThrow(new IllegalStateException("inventory fail"))
                .when(registerInventoryPort).registerInventory(30L, 200L);

        assertThatThrownBy(() -> registerProductService.registerProduct(command))
                .isInstanceOf(IllegalStateException.class);

        verify(publishProductEventPort).publishProductRegisteredEvent(30L, 200L, command.sellerId());
        verify(registerFulfillmentVendorGoodsPort, never()).registerFulfillmentVendorGoods(any());
    }

    @Test
    @DisplayName("상품 등록 시 풀필먼트 필수 옵션이 누락되면 예외를 던진다")
    void registerProduct_missingFulfillmentRequiredFields_throws() {
        FulfillmentVendorGoodsOptionCommand options = FulfillmentVendorGoodsOptionCommand.builder()
                .giftDiv("01")
                .build();
        RegisterProductCommand command = buildCommand(options);
        Product savedProduct = buildSavedProduct(40L, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenReturn(RegisterPricePolicyResult.of(300L));

        assertThatThrownBy(() -> registerProductService.registerProduct(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("godType");

        verify(publishProductEventPort).publishProductRegisteredEvent(40L, 300L, command.sellerId());
        verify(registerInventoryPort).registerInventory(40L, 300L);
        verify(registerFulfillmentVendorGoodsPort, never()).registerFulfillmentVendorGoods(any());
    }

    @Test
    @DisplayName("상품 등록 시 저장된 상품에 ID가 없으면 풀필먼트 매핑에서 예외를 던진다")
    void registerProduct_missingProductId_throws() {
        RegisterProductCommand command = buildCommand(null);
        Product savedProduct = buildSavedProduct(null, command);

        when(saveProductPort.save(any(Product.class))).thenReturn(savedProduct);
        when(registerPricePolicyUseCase.registerPricePolicy(
                eq(command.sellerId()), eq(false), any(RegisterPricePolicyCommand.class)
        )).thenReturn(RegisterPricePolicyResult.of(400L));

        assertThatThrownBy(() -> registerProductService.registerProduct(command))
                .isInstanceOf(ProductInfoNoValueException.class)
                .hasMessageContaining("상품 ID가 존재하지 않습니다.");

        verify(publishProductEventPort).publishProductRegisteredEvent(null, 400L, command.sellerId());
        verify(registerInventoryPort).registerInventory(null, 400L);
        verify(registerFulfillmentVendorGoodsPort, never()).registerFulfillmentVendorGoods(any());
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
                .godType("2")
                .giftDiv("99")
                .godOptCd1("opt1")
                .godOptCd2("opt2")
                .invGodNmUseYn("Y")
                .invGodNm("상품명")
                .supCd("SUP")
                .cateCd("CATE")
                .seasonCd("2024")
                .genderCd("M")
                .makeYr("2024")
                .godPr("12000")
                .inPr("9000")
                .salPr("10000")
                .dealTemp("ROOM")
                .pickFac("A1")
                .godBarcd("BARCODE")
                .boxWeight("2.5")
                .origin("KR")
                .distTermMgtYn("Y")
                .useTermDay("30")
                .outCanDay("7")
                .inCanDay("7")
                .boxDiv("BOX")
                .bufGodYn("N")
                .loadingDirection("UP")
                .subMate("COTTON")
                .useYn("Y")
                .safetyStock("10")
                .feeYn("N")
                .saleUnitQty("1")
                .cstGodImgUrl("https://example.com/image.jpg")
                .externalGodImgUrl("https://example.com/external.jpg")
                .build();
    }

    private RegisterFulfillmentVendorGoodsCommand buildExpectedFulfillmentCommand(
            Product product,
            FulfillmentVendorGoodsOptionCommand options
    ) {
        return RegisterFulfillmentVendorGoodsCommand.builder()
                .cstGodCd(String.valueOf(product.getId()))
                .godNm(product.getName())
                .godType(options.godType())
                .giftDiv(options.giftDiv())
                .godOptCd1(options.godOptCd1())
                .godOptCd2(options.godOptCd2())
                .invGodNmUseYn(options.invGodNmUseYn())
                .invGodNm(options.invGodNm())
                .supCd(options.supCd())
                .cateCd(options.cateCd())
                .seasonCd(options.seasonCd())
                .genderCd(options.genderCd())
                .makeYr(options.makeYr())
                .godPr(options.godPr())
                .inPr(options.inPr())
                .salPr(options.salPr())
                .dealTemp(options.dealTemp())
                .pickFac(options.pickFac())
                .godBarcd(options.godBarcd())
                .boxWeight(options.boxWeight())
                .origin(options.origin())
                .distTermMgtYn(options.distTermMgtYn())
                .useTermDay(options.useTermDay())
                .outCanDay(options.outCanDay())
                .inCanDay(options.inCanDay())
                .boxDiv(options.boxDiv())
                .bufGodYn(options.bufGodYn())
                .loadingDirection(options.loadingDirection())
                .subMate(options.subMate())
                .useYn(options.useYn())
                .safetyStock(options.safetyStock())
                .feeYn(options.feeYn())
                .saleUnitQty(options.saleUnitQty())
                .cstGodImgUrl(options.cstGodImgUrl())
                .externalGodImgUrl(options.externalGodImgUrl())
                .build();
    }
}
