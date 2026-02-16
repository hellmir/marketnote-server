package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.inventory.RegisterInventoryPort;
import com.personal.marketnote.product.port.out.pricepolicy.SavePricePolicyPort;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import com.personal.marketnote.product.port.out.productoption.UpdateOptionPricePolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPricePolicyUseCaseTest {
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;
    @Mock
    private SavePricePolicyPort savePricePolicyPort;
    @Mock
    private UpdateOptionPricePolicyPort updateOptionPricePolicyPort;
    @Mock
    private RegisterInventoryPort registerInventoryPort;

    @InjectMocks
    private RegisterPricePolicyService registerPricePolicyService;

    @Test
    @DisplayName("가격 정책 등록 요청 회원이 관리자 또는 상품 판매자가 아니면 예외를 던진다")
    void registerPricePolicy_notOwner_throws() {
        Long userId = 1L;
        RegisterPricePolicyCommand command = buildCommand(10L, List.of(1L));

        when(findProductPort.existsByIdAndSellerId(10L, userId)).thenReturn(false);

        assertThatThrownBy(() -> registerPricePolicyService.registerPricePolicy(userId, false, command))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(10L, userId);
        verifyNoInteractions(
                getProductUseCase,
                savePricePolicyPort,
                updateOptionPricePolicyPort,
                registerInventoryPort
        );
    }

    @Test
    @DisplayName("가격 정책 등록 시 판매자 요청이면 가격 정책을 저장하고 옵션에 할당한다")
    void registerPricePolicy_owner_withOptions_assignsOptions() {
        Long userId = 2L;
        Long productId = 20L;
        List<Long> optionIds = List.of(101L, 102L);
        RegisterPricePolicyCommand command = buildCommand(productId, optionIds);
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenReturn(100L);

        RegisterPricePolicyResult result = registerPricePolicyService.registerPricePolicy(userId, false, command);

        assertThat(result.id()).isEqualTo(100L);
        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);

        ArgumentCaptor<PricePolicy> captor = ArgumentCaptor.forClass(PricePolicy.class);
        verify(savePricePolicyPort).save(captor.capture());
        PricePolicy saved = captor.getValue();

        assertThat(saved.getProduct()).isSameAs(product);
        assertThat(saved.getPrice()).isEqualTo(command.price());
        assertThat(saved.getDiscountPrice()).isEqualTo(command.discountPrice());
        assertThat(saved.getAccumulatedPoint()).isEqualTo(command.accumulatedPoint());
        assertThat(saved.getOptionIds()).containsExactlyElementsOf(optionIds);
        assertThat(saved.getDiscountRate())
                .isEqualByComparingTo(calculateDiscountRate(command.price(), command.discountPrice()));
        assertThat(saved.getAccumulationRate())
                .isEqualByComparingTo(calculateAccumulationRate(command.accumulatedPoint(), command.discountPrice()));

        verify(updateOptionPricePolicyPort).assignPricePolicyToOptions(productId, 100L, optionIds);
        verify(registerInventoryPort).registerInventory(productId, 100L);
    }

    @Test
    @DisplayName("가격 정책 등록 시 관리자 요청이면 소유자 확인 없이 등록한다")
    void registerPricePolicy_admin_skipsOwnerCheck() {
        Long userId = 3L;
        Long productId = 30L;
        RegisterPricePolicyCommand command = buildCommand(productId, null);
        Product product = buildProduct(productId, 999L);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenReturn(200L);

        RegisterPricePolicyResult result = registerPricePolicyService.registerPricePolicy(userId, true, command);

        assertThat(result.id()).isEqualTo(200L);
        verify(registerInventoryPort).registerInventory(productId, 200L);
        verify(updateOptionPricePolicyPort, never()).assignPricePolicyToOptions(anyLong(), anyLong(), anyList());
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("가격 정책 등록 시 옵션 ID가 비어 있으면 옵션에 가격 정책을 할당하지 않는다")
    void registerPricePolicy_emptyOptionIds_skipsAssign() {
        Long userId = 4L;
        Long productId = 40L;
        RegisterPricePolicyCommand command = buildCommand(productId, List.of());
        Product product = buildProduct(productId, userId);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenReturn(250L);

        registerPricePolicyService.registerPricePolicy(userId, false, command);

        verify(updateOptionPricePolicyPort, never()).assignPricePolicyToOptions(anyLong(), anyLong(), anyList());
        verify(registerInventoryPort).registerInventory(productId, 250L);
    }

    @Test
    @DisplayName("가격 정책 등록 시 상품 조회에 실패하면 예외를 전파한다")
    void registerPricePolicy_getProductFails_propagates() {
        Long userId = 5L;
        Long productId = 50L;
        RegisterPricePolicyCommand command = buildCommand(productId, List.of(201L));
        RuntimeException exception = new RuntimeException("product fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> registerPricePolicyService.registerPricePolicy(userId, false, command))
                .isSameAs(exception);

        verify(savePricePolicyPort, never()).save(any(PricePolicy.class));
        verifyNoInteractions(updateOptionPricePolicyPort, registerInventoryPort);
    }

    @Test
    @DisplayName("가격 정책 등록 시 저장에 실패하면 이후 작업을 수행하지 않는다")
    void registerPricePolicy_saveFails_stopsFlow() {
        Long userId = 6L;
        Long productId = 60L;
        RegisterPricePolicyCommand command = buildCommand(productId, List.of(301L));
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("save fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenThrow(exception);

        assertThatThrownBy(() -> registerPricePolicyService.registerPricePolicy(userId, false, command))
                .isSameAs(exception);

        verify(updateOptionPricePolicyPort, never()).assignPricePolicyToOptions(anyLong(), anyLong(), anyList());
        verifyNoInteractions(registerInventoryPort);
    }

    @Test
    @DisplayName("가격 정책 등록 시 옵션 가격 정책 할당에 실패하면 예외를 전파한다")
    void registerPricePolicy_updateOptionsFails_propagates() {
        Long userId = 7L;
        Long productId = 70L;
        List<Long> optionIds = List.of(401L);
        RegisterPricePolicyCommand command = buildCommand(productId, optionIds);
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("option fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenReturn(300L);
        doThrow(exception).when(updateOptionPricePolicyPort).assignPricePolicyToOptions(productId, 300L, optionIds);

        assertThatThrownBy(() -> registerPricePolicyService.registerPricePolicy(userId, false, command))
                .isSameAs(exception);

        verify(registerInventoryPort, never()).registerInventory(anyLong(), anyLong());
    }

    @Test
    @DisplayName("가격 정책 등록 시 재고 등록에 실패하면 예외를 전파한다")
    void registerPricePolicy_registerInventoryFails_propagates() {
        Long userId = 8L;
        Long productId = 80L;
        List<Long> optionIds = List.of(501L);
        RegisterPricePolicyCommand command = buildCommand(productId, optionIds);
        Product product = buildProduct(productId, userId);
        RuntimeException exception = new RuntimeException("inventory fail");

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);
        when(savePricePolicyPort.save(any(PricePolicy.class))).thenReturn(400L);
        doThrow(exception).when(registerInventoryPort).registerInventory(productId, 400L);

        assertThatThrownBy(() -> registerPricePolicyService.registerPricePolicy(userId, false, command))
                .isSameAs(exception);

        verify(updateOptionPricePolicyPort).assignPricePolicyToOptions(productId, 400L, optionIds);
    }

    private RegisterPricePolicyCommand buildCommand(Long productId, List<Long> optionIds) {
        return RegisterPricePolicyCommand.of(
                productId,
                10000L,
                8000L,
                200L,
                optionIds
        );
    }

    private Product buildProduct(Long productId, Long sellerId) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(productId)
                        .sellerId(sellerId)
                        .name("테스트 상품")
                        .brandName("브랜드")
                        .detail("상세 설명")
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }

    private BigDecimal calculateDiscountRate(Long price, Long discountPrice) {
        BigDecimal priceValue = BigDecimal.valueOf(price);
        BigDecimal discountValue = BigDecimal.valueOf(discountPrice);
        BigDecimal hundred = new BigDecimal("100");

        return priceValue.subtract(discountValue)
                .divide(priceValue, 3, RoundingMode.HALF_UP)
                .multiply(hundred)
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAccumulationRate(Long accumulatedPoint, Long discountPrice) {
        BigDecimal discountValue = BigDecimal.valueOf(discountPrice);
        BigDecimal hundred = new BigDecimal("100");

        return BigDecimal.valueOf(accumulatedPoint)
                .divide(discountValue, 3, RoundingMode.HALF_UP)
                .multiply(hundred)
                .setScale(1, RoundingMode.HALF_UP);
    }
}
