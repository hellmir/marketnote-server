package com.personal.marketnote.product.service.cart;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.cart.CartProduct;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.port.in.command.AddCartProductCommand;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePolicyUseCase;
import com.personal.marketnote.product.port.out.cart.SaveCartProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCartProductUseCaseTest {
    @Mock
    private GetPricePolicyUseCase getPricePolicyUseCase;
    @Mock
    private SaveCartProductPort saveCartProductPort;

    @InjectMocks
    private AddCartProductService addCartProductService;

    @Test
    @DisplayName("장바구니 추가 시 가격 정책을 조회하고 장바구니 상품을 저장한다")
    void addCartProduct_success_savesCartProduct() {
        AddCartProductCommand command = AddCartProductCommand.builder()
                .userId(1L)
                .sharerId(2L)
                .pricePolicyId(100L)
                .imageUrl("https://example.com/image.png")
                .quantity((short) 3)
                .build();
        PricePolicy pricePolicy = buildPricePolicy(100L);

        when(getPricePolicyUseCase.getPricePolicy(100L)).thenReturn(pricePolicy);
        when(saveCartProductPort.save(any(CartProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        addCartProductService.addCartProduct(command);

        ArgumentCaptor<CartProduct> captor = ArgumentCaptor.forClass(CartProduct.class);
        verify(saveCartProductPort).save(captor.capture());
        CartProduct saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getSharerId()).isEqualTo(2L);
        assertThat(saved.getPricePolicy()).isSameAs(pricePolicy);
        assertThat(saved.getPricePolicyId()).isEqualTo(100L);
        assertThat(saved.getImageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(saved.getQuantity()).isEqualTo((short) 3);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("장바구니 상품 추가 시 가격 정책 조회에 실패하면 예외를 전파한다")
    void addCartProduct_getPricePolicyFails_propagates() {
        AddCartProductCommand command = AddCartProductCommand.builder()
                .userId(1L)
                .sharerId(2L)
                .pricePolicyId(101L)
                .imageUrl("https://example.com/image.png")
                .quantity((short) 1)
                .build();
        RuntimeException exception = new RuntimeException("price policy fail");

        when(getPricePolicyUseCase.getPricePolicy(101L)).thenThrow(exception);

        assertThatThrownBy(() -> addCartProductService.addCartProduct(command))
                .isSameAs(exception);

        verifyNoInteractions(saveCartProductPort);
    }

    @Test
    @DisplayName("장바구니 상품 추가 시 저장에 실패하면 예외를 전파한다")
    void addCartProduct_saveFails_propagates() {
        AddCartProductCommand command = AddCartProductCommand.builder()
                .userId(1L)
                .sharerId(2L)
                .pricePolicyId(102L)
                .imageUrl("https://example.com/image.png")
                .quantity((short) 2)
                .build();
        PricePolicy pricePolicy = buildPricePolicy(102L);
        RuntimeException exception = new RuntimeException("save fail");

        when(getPricePolicyUseCase.getPricePolicy(102L)).thenReturn(pricePolicy);
        when(saveCartProductPort.save(any(CartProduct.class))).thenThrow(exception);

        assertThatThrownBy(() -> addCartProductService.addCartProduct(command))
                .isSameAs(exception);
    }

    private PricePolicy buildPricePolicy(Long id) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(new BigDecimal("20.0"))
                        .accumulatedPoint(200L)
                        .accumulationRate(new BigDecimal("2.5"))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
