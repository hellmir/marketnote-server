package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.commerce.port.out.payment.SavePaymentPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.commerce.port.out.settlement.SavePaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.shipping.FindShippingPolicyBySellerIdsPort;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InsufficientQuantityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterOrderUseCaseTest {
    @Mock
    private GetInventoryUseCase getInventoryUseCase;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private SaveOrderPort saveOrderPort;
    @Mock
    private SavePaymentPort savePaymentPort;
    @Mock
    private SavePaymentAllocationPort savePaymentAllocationPort;
    @Mock
    private FindShippingPolicyBySellerIdsPort findShippingPolicyBySellerIdsPort;
    @Mock
    private ModifyUserPointPort modifyUserPointPort;

    @InjectMocks
    private RegisterOrderService registerOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(findShippingPolicyBySellerIdsPort.findBySellerIds(anyList()))
                .thenReturn(Map.of());
    }

    // ==================================================================================
    // 성공 케이스
    // ==================================================================================

    @Nested
    @DisplayName("주문 등록 성공 케이스")
    class RegisterOrderSuccessTest {

        @Test
        @DisplayName("단일 상품 주문 등록 시 주문이 저장되고 결과를 반환한다")
        void registerOrder_singleProduct_savesAndReturnsResult() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(5L)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .imageUrl("https://example.com/image.png")
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 25000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("복수 상품 주문 등록 시 모든 상품을 포함한 주문이 저장된다")
        void registerOrder_multipleProducts_savesAllProducts() {
            Long buyerId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(100000L)
                    .couponAmount(5000L)
                    .pointAmount(3000L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .sharerId(5L)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .imageUrl("https://example.com/image1.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .sharerId(6L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image2.png")
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(25000L, 10L),
                    pricePolicyId2, Map.entry(50000L, 20L)
            ));
            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(999999L);

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 50);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory1, inventory2));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(2);
        }

        @Test
        @DisplayName("쿠폰 할인이 적용된 주문 등록 시 쿠폰 금액이 저장된다")
        void registerOrder_withCoupon_savesCouponAmount() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long couponAmount = 10000L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(couponAmount)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getCouponAmount()).isEqualTo(couponAmount);
        }

        @Test
        @DisplayName("포인트가 사용된 주문 등록 시 포인트 금액이 저장된다")
        void registerOrder_withPoint_savesPointAmount() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long pointAmount = 5000L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(pointAmount)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);
            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(999999L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getPointAmount()).isEqualTo(pointAmount);
        }

        @Test
        @DisplayName("쿠폰과 포인트가 동시 사용된 주문 등록 시 두 금액이 모두 저장된다")
        void registerOrder_withCouponAndPoint_savesBothAmounts() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long couponAmount = 10000L;
            Long pointAmount = 5000L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(couponAmount)
                    .pointAmount(pointAmount)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);
            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(999999L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getCouponAmount()).isEqualTo(couponAmount);
            assertThat(capturedOrder.getPointAmount()).isEqualTo(pointAmount);
        }

        @Test
        @DisplayName("sharerId가 null인 주문 상품 등록 시 sharerId가 null로 저장된다")
        void registerOrder_nullSharerId_savesWithNullSharerId() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(null)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image.png")
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getSharerId()).isNull();
        }

        @Test
        @DisplayName("imageUrl이 null인 주문 상품 등록 시 imageUrl이 null로 저장된다")
        void registerOrder_nullImageUrl_savesWithNullImageUrl() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(5L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl(null)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getImageUrl()).isNull();
        }

        @Test
        @DisplayName("couponAmount가 null인 주문 등록 시 couponAmount가 null로 저장된다")
        void registerOrder_nullCouponAmount_savesWithNullCouponAmount() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(null)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getCouponAmount()).isNull();
        }

        @Test
        @DisplayName("pointAmount가 null인 주문 등록 시 pointAmount가 null로 저장된다")
        void registerOrder_nullPointAmount_savesWithNullPointAmount() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(null)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getPointAmount()).isNull();
        }
    }

    // ==================================================================================
    // Order 생성 및 속성 검증
    // ==================================================================================

    @Nested
    @DisplayName("Order 생성 및 속성 검증")
    class OrderCreationTest {

        @Test
        @DisplayName("주문 등록 시 buyerId가 정확하게 저장된다")
        void registerOrder_savesBuyerIdCorrectly() {
            Long buyerId = 123L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getBuyerId()).isEqualTo(buyerId);
        }

        @Test
        @DisplayName("주문 등록 시 totalAmount가 정확하게 저장된다")
        void registerOrder_savesTotalAmountCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long totalAmount = 150000L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 150000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 150000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getTotalAmount()).isEqualTo(totalAmount);
        }

        @Test
        @DisplayName("주문 등록 시 초기 상태가 PAYMENT_PENDING으로 설정된다")
        void registerOrder_setsInitialStatusToPaymentPending() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("주문 등록 시 orderKey가 자동 생성된다")
        void registerOrder_generatesOrderKey() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderKey()).isNotNull();
        }

        @Test
        @DisplayName("주문 등록 시 orderNumber가 자동 생성된다")
        void registerOrder_generatesOrderNumber() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderNumber()).isNotNull();
            assertThat(capturedOrder.getOrderNumber()).isNotEmpty();
        }
    }

    // ==================================================================================
    // OrderProduct 생성 및 속성 검증
    // ==================================================================================

    @Nested
    @DisplayName("OrderProduct 생성 및 속성 검증")
    class OrderProductCreationTest {

        @Test
        @DisplayName("주문 상품의 sellerId가 정확하게 저장된다")
        void registerOrder_savesSellerIdCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long sellerId = 99L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(sellerId)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPriceWithSellerId(pricePolicyId, 50000L, sellerId);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getSellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("주문 상품의 pricePolicyId가 정확하게 저장된다")
        void registerOrder_savesPricePolicyIdCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 777L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getPricePolicyId()).isEqualTo(pricePolicyId);
        }

        @Test
        @DisplayName("주문 상품의 quantity가 정확하게 저장된다")
        void registerOrder_savesQuantityCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Integer quantity = 5;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, quantity, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("주문 상품의 unitAmount가 정확하게 저장된다")
        void registerOrder_savesUnitAmountCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long unitAmount = 75000L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, unitAmount, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, unitAmount);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getUnitAmount()).isEqualTo(unitAmount);
        }

        @Test
        @DisplayName("주문 상품의 imageUrl이 정확하게 저장된다")
        void registerOrder_savesImageUrlCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            String imageUrl = "https://marketnote.example.com/images/product-123.png";
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl(imageUrl)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getImageUrl()).isEqualTo(imageUrl);
        }

        @Test
        @DisplayName("주문 상품의 sharerId가 정확하게 저장된다")
        void registerOrder_savesSharerIdCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long sharerId = 55L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(sharerId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getSharerId()).isEqualTo(sharerId);
        }

        @Test
        @DisplayName("주문 상품의 초기 상태가 PAYMENT_PENDING으로 설정된다")
        void registerOrder_setsOrderProductStatusToPaymentPending() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getOrderStatus())
                    .isEqualTo(OrderStatus.PAYMENT_PENDING);
        }
    }

    // ==================================================================================
    // 주문 금액 계산식 검증
    // ==================================================================================

    @Nested
    @DisplayName("주문 금액 계산식 검증")
    class OrderAmountValidationTest {

        @Test
        @DisplayName("totalAmount가 상품별 금액 합계와 일치하면 주문이 성공한다")
        void registerOrder_matchingTotalAmount_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 2, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("totalAmount가 상품별 금액 합계보다 작으면 OrderAmountMismatchException이 발생한다")
        void registerOrder_totalAmountLessThanCalculated_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(30000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class)
                    .hasMessageContaining("주문 총액이 상품 금액 합계와 일치하지 않습니다");
        }

        @Test
        @DisplayName("totalAmount가 상품별 금액 합계보다 크면 OrderAmountMismatchException이 발생한다")
        void registerOrder_totalAmountGreaterThanCalculated_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);
        }

        @Test
        @DisplayName("totalAmount가 0이고 상품 금액이 0이 아니면 OrderAmountMismatchException이 발생한다")
        void registerOrder_zeroTotalAmountWithNonZeroProducts_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(0L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);
        }

        @Test
        @DisplayName("복수 상품의 금액 합계가 totalAmount와 불일치하면 OrderAmountMismatchException이 발생한다")
        void registerOrder_multipleProductsMismatch_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(99999L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(200L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);
        }

        @Test
        @DisplayName("금액 불일치 시 재고 검증/주문 저장이 호출되지 않는다")
        void registerOrder_amountMismatch_doesNotCallSubsequentPorts() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(1L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);

            verifyNoInteractions(getInventoryUseCase, saveOrderPort, savePaymentPort, findProductByPricePolicyPort);
        }

        @Test
        @DisplayName("unitAmount * quantity 오버플로우 발생 시 OrderAmountMismatchException이 발생한다")
        void registerOrder_overflowInMultiplication_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(Long.MAX_VALUE)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(2)
                                    .unitAmount(Long.MAX_VALUE)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);
        }

        @Test
        @DisplayName("복수 상품 합산 시 오버플로우 발생하면 OrderAmountMismatchException이 발생한다")
        void registerOrder_overflowInSum_throwsException() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(Long.MAX_VALUE)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(100L)
                                    .quantity(1)
                                    .unitAmount(Long.MAX_VALUE)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(101L)
                                    .sellerId(10L)
                                    .pricePolicyId(101L)
                                    .quantity(1)
                                    .unitAmount(Long.MAX_VALUE)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(OrderAmountMismatchException.class);
        }
    }

    // ==================================================================================
    // 실제 상품 가격 검증
    // ==================================================================================

    @Nested
    @DisplayName("실제 상품 가격 검증")
    class PriceValidationTest {

        @Test
        @DisplayName("unitAmount가 실제 가격과 일치하면 주문이 성공한다")
        void registerOrder_matchingPrice_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("unitAmount가 실제 가격보다 낮으면 PriceMismatchException이 발생한다")
        void registerOrder_unitAmountLowerThanActual_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 1L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(PriceMismatchException.class)
                    .hasMessageContaining("주문 상품 단가가 실제 가격과 일치하지 않습니다");
        }

        @Test
        @DisplayName("unitAmount가 실제 가격보다 높으면 PriceMismatchException이 발생한다")
        void registerOrder_unitAmountHigherThanActual_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 100000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(PriceMismatchException.class);
        }

        @Test
        @DisplayName("discountPrice가 존재하면 discountPrice로 검증한다")
        void registerOrder_withDiscountPrice_validatesAgainstDiscountPrice() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 40000L, 1, 0L, 0L);

            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(pricePolicyId,
                            new ProductInfoResult(1L, 10L, "상품", "브랜드", 50000L, 40000L, null, List.of())));
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("discountPrice가 null이면 price로 검증한다")
        void registerOrder_nullDiscountPrice_validatesAgainstPrice() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of(pricePolicyId,
                            new ProductInfoResult(1L, 10L, "상품", "브랜드", 50000L, null, null, List.of())));
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("product-service 조회 실패(빈 Map) 시 가격 검증을 건너뛴다")
        void registerOrder_productServiceFailure_skipsValidation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(Map.of());
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("가격 불일치 시 재고 검증이 호출되지 않는다")
        void registerOrder_priceMismatch_doesNotCallInventory() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 1L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(PriceMismatchException.class);

            verifyNoInteractions(getInventoryUseCase, saveOrderPort, savePaymentPort);
        }
    }

    // ==================================================================================
    // sellerId 교차 검증
    // ==================================================================================

    @Nested
    @DisplayName("sellerId 교차 검증")
    class SellerIdValidationTest {

        @Test
        @DisplayName("sellerId가 실제 상품의 판매자와 일치하면 주문이 성공한다")
        void registerOrder_matchingSellerId_succeeds() {
            Long pricePolicyId = 100L;
            Long sellerId = 10L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(sellerId)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPriceWithSellerId(pricePolicyId, 50000L, sellerId);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sellerId가 실제 상품의 판매자와 다르면 SellerMismatchException이 발생한다")
        void registerOrder_mismatchingSellerId_throwsException() {
            Long pricePolicyId = 100L;
            Long requestedSellerId = 999L;
            Long actualSellerId = 10L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(requestedSellerId)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPriceWithSellerId(pricePolicyId, 50000L, actualSellerId);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(SellerMismatchException.class)
                    .hasMessageContaining("판매자가 실제 상품의 판매자와 일치하지 않습니다");
        }

        @Test
        @DisplayName("product-service 응답의 sellerId가 null이면 검증을 건너뛴다")
        void registerOrder_nullActualSellerId_skipsValidation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(999L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPriceWithSellerId(pricePolicyId, 50000L, null);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("sellerId 불일치 시 재고 검증/주문 저장이 호출되지 않는다")
        void registerOrder_sellerMismatch_doesNotCallSubsequentPorts() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(999L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPriceWithSellerId(pricePolicyId, 50000L, 10L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(SellerMismatchException.class);

            verifyNoInteractions(getInventoryUseCase, saveOrderPort, savePaymentPort);
        }

        @Test
        @DisplayName("복수 상품 중 하나의 sellerId가 불일치하면 SellerMismatchException이 발생한다")
        void registerOrder_oneSellerMismatch_throwsException() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(999L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            java.util.HashMap<Long, ProductInfoResult> productInfoMap = new java.util.HashMap<>();
            productInfoMap.put(pricePolicyId1, new ProductInfoResult(1L, 10L, "상품1", "브랜드1", 50000L, null, null, List.of()));
            productInfoMap.put(pricePolicyId2, new ProductInfoResult(2L, 20L, "상품2", "브랜드2", 50000L, null, null, List.of()));
            when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                    .thenReturn(productInfoMap);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(SellerMismatchException.class);
        }
    }

    // ==================================================================================
    // 할인 금액 검증
    // ==================================================================================

    @Nested
    @DisplayName("할인 금액 검증")
    class DiscountValidationTest {

        @Test
        @DisplayName("couponAmount + pointAmount가 totalAmount 이하이면 성공한다")
        void registerOrder_validDiscount_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 10000L, 5000L);

            mockProductPrice(pricePolicyId, 50000L);
            when(modifyUserPointPort.getAvailablePoints(1L)).thenReturn(999999L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("couponAmount가 totalAmount를 초과하면 ExcessiveDiscountException이 발생한다")
        void registerOrder_excessiveCoupon_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 60000L, 0L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(ExcessiveDiscountException.class)
                    .hasMessageContaining("할인 금액이 주문 총액 + 배송비를 초과합니다");
        }

        @Test
        @DisplayName("pointAmount가 totalAmount를 초과하면 ExcessiveDiscountException이 발생한다")
        void registerOrder_excessivePoint_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 200000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(ExcessiveDiscountException.class);
        }

        @Test
        @DisplayName("couponAmount + pointAmount 합계가 totalAmount를 초과하면 ExcessiveDiscountException이 발생한다")
        void registerOrder_combinedDiscountExceedsTotalAmount_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 30000L, 30000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(ExcessiveDiscountException.class);
        }

        @Test
        @DisplayName("couponAmount와 pointAmount가 null이면 할인 검증을 통과한다")
        void registerOrder_nullDiscountAmounts_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(null)
                    .pointAmount(null)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("할인 초과 시 가격 검증/재고 검증이 호출되지 않는다")
        void registerOrder_excessiveDiscount_doesNotCallSubsequentPorts() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 60000L, 0L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(ExcessiveDiscountException.class);

            verifyNoInteractions(findProductByPricePolicyPort, getInventoryUseCase, saveOrderPort, savePaymentPort);
        }

        @Test
        @DisplayName("couponAmount + pointAmount 오버플로우 시 ExcessiveDiscountException이 발생한다")
        void registerOrder_discountOverflow_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(Long.MAX_VALUE / 2 + 1)
                    .pointAmount(Long.MAX_VALUE / 2 + 1)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(ExcessiveDiscountException.class);
        }
    }

    // ==================================================================================
    // 포인트 잔액 검증 케이스
    // ==================================================================================

    @Nested
    @DisplayName("포인트 잔액 검증 케이스")
    class PointBalanceValidationTest {

        @Test
        @DisplayName("포인트 사용 시 잔액이 충분하면 주문이 성공한다")
        void registerOrder_sufficientPoint_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long pointAmount = 5000L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, pointAmount);

            mockProductPrice(pricePolicyId, 50000L);
            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(10000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();

            verify(modifyUserPointPort).getAvailablePoints(buyerId);
        }

        @Test
        @DisplayName("포인트 사용 시 잔액이 부족하면 InsufficientPointException이 발생한다")
        void registerOrder_insufficientPoint_throwsException() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long pointAmount = 10000L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, pointAmount);

            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(5000L);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientPointException.class);

            verifyNoInteractions(getInventoryUseCase, saveOrderPort, savePaymentPort);
        }

        @Test
        @DisplayName("포인트 사용 시 잔액이 정확히 일치하면 주문이 성공한다")
        void registerOrder_exactPoint_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long pointAmount = 5000L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, pointAmount);

            mockProductPrice(pricePolicyId, 50000L);
            when(modifyUserPointPort.getAvailablePoints(buyerId)).thenReturn(5000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            assertThatCode(() -> registerOrderService.registerOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("포인트를 사용하지 않으면 잔액 조회를 하지 않는다")
        void registerOrder_zeroPoint_skipsBalanceCheck() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            verify(modifyUserPointPort, never()).getAvailablePoints(any());
        }

        @Test
        @DisplayName("포인트가 null이면 잔액 조회를 하지 않는다")
        void registerOrder_nullPoint_skipsBalanceCheck() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(buyerId, pricePolicyId, 50000L, 1, 0L, null);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            verify(modifyUserPointPort, never()).getAvailablePoints(any());
        }
    }

    // ==================================================================================
    // 재고 검증 케이스
    // ==================================================================================

    @Nested
    @DisplayName("재고 검증 케이스")
    class InventoryValidationTest {

        @Test
        @DisplayName("주문 등록 시 재고 조회가 호출된다")
        void registerOrder_callsGetInventories() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(50000L, 10L),
                    pricePolicyId2, Map.entry(50000L, 20L)
            ));

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory1, inventory2));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            verify(getInventoryUseCase).getOrCreateInventories(anyMap());
        }

        @Test
        @DisplayName("재고가 충분하면 주문이 정상 처리된다")
        void registerOrder_sufficientStock_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(250000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(5)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 10);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(saveOrderPort).save(any(Order.class));
        }

        @Test
        @DisplayName("재고가 주문 수량과 정확히 일치하면 주문이 정상 처리된다")
        void registerOrder_exactStock_succeeds() {
            Long pricePolicyId = 100L;
            int orderQuantity = 10;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(orderQuantity)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, orderQuantity);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            verify(saveOrderPort).save(any(Order.class));
        }

        @Test
        @DisplayName("재고가 부족하면 InsufficientQuantityException이 발생한다")
        void registerOrder_insufficientStock_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(10)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 5);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class)
                    .hasMessageContaining("재고 수량이 부족합니다");

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("재고가 0이고 주문 수량이 1 이상이면 InsufficientQuantityException이 발생한다")
        void registerOrder_zeroStock_throwsException() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 0);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("복수 상품 중 하나라도 재고가 부족하면 InsufficientQuantityException이 발생한다")
        void registerOrder_oneProductInsufficientStock_throwsException() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(5)
                                    .unitAmount(10000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(10)
                                    .unitAmount(5000L)
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(10000L, 10L),
                    pricePolicyId2, Map.entry(5000L, 20L)
            ));

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 5);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory1, inventory2));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("동일 pricePolicyId의 복수 상품이 있으면 수량을 합산하여 재고 검증한다")
        void registerOrder_samePricePolicyMultipleItems_sumsQuantityForValidation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(350000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(3)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(4)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 6);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class)
                    .hasMessageContaining("현재 재고 수량: 6")
                    .hasMessageContaining("주문 수량: 7");
        }

        @Test
        @DisplayName("동일 pricePolicyId의 복수 상품이 있고 합산 재고가 충분하면 성공한다")
        void registerOrder_samePricePolicyMultipleItems_sufficientStock_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(350000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(3)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(4)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 10);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            verify(saveOrderPort).save(any(Order.class));
        }
    }

    // ==================================================================================
    // 호출 순서 검증
    // ==================================================================================

    @Nested
    @DisplayName("호출 순서 검증")
    class InvocationOrderTest {

        @Test
        @DisplayName("주문 등록 시 가격 검증 -> 재고 조회 -> 주문 저장 -> 결제 저장 순서로 호출한다")
        void registerOrder_callsInOrder() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            InOrder inOrder = inOrder(findProductByPricePolicyPort, getInventoryUseCase, saveOrderPort, savePaymentPort);
            inOrder.verify(findProductByPricePolicyPort).findByPricePolicyIds(anyList());
            inOrder.verify(getInventoryUseCase).getOrCreateInventories(anyMap());
            inOrder.verify(saveOrderPort).save(any(Order.class));
            inOrder.verify(savePaymentPort).save(any(Payment.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("재고 검증 실패 시 SaveOrderPort가 호출되지 않는다")
        void registerOrder_stockValidationFails_doesNotCallSaveOrderPort() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(10)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 5);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(getInventoryUseCase).getOrCreateInventories(anyMap());
            verifyNoInteractions(saveOrderPort);
        }
    }

    // ==================================================================================
    // 예외 전파 케이스
    // ==================================================================================

    @Nested
    @DisplayName("예외 전파 케이스")
    class ExceptionPropagationTest {

        @Test
        @DisplayName("재고 조회 중 예외 발생 시 예외를 전파한다")
        void registerOrder_getInventoriesFails_propagates() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            RuntimeException exception = new RuntimeException("재고 조회 실패");
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isSameAs(exception);

            verifyNoInteractions(saveOrderPort);
        }

        @Test
        @DisplayName("주문 저장 중 예외 발생 시 예외를 전파한다")
        void registerOrder_saveOrderFails_propagates() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            RuntimeException exception = new RuntimeException("주문 저장 실패");
            when(saveOrderPort.save(any(Order.class))).thenThrow(exception);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 조회 실패 시 재고 검증이 수행되지 않는다")
        void registerOrder_getInventoriesFails_noValidation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenThrow(new RuntimeException("재고 조회 실패"));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고 조회 실패");

            verifyNoInteractions(saveOrderPort);
        }
    }

    // ==================================================================================
    // 결과 반환 검증
    // ==================================================================================

    @Nested
    @DisplayName("결과 반환 검증")
    class ResultVerificationTest {

        @Test
        @DisplayName("주문 등록 성공 시 저장된 주문의 ID가 포함된 결과를 반환한다")
        void registerOrder_success_returnsOrderId() {
            Long pricePolicyId = 100L;
            Long expectedOrderId = 999L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(expectedOrderId);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(expectedOrderId);
        }

        @Test
        @DisplayName("서로 다른 주문 등록 시 각각 다른 ID가 반환된다")
        void registerOrder_differentOrders_returnsDifferentIds() {
            Long pricePolicyId = 100L;

            RegisterOrderCommand command1 = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);
            RegisterOrderCommand command2 = RegisterOrderCommand.builder()
                    .buyerId(2L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(2)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 50000L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder1 = mockSavedOrder(1L);
            Order savedOrder2 = mockSavedOrder(2L);
            when(saveOrderPort.save(any(Order.class)))
                    .thenReturn(savedOrder1)
                    .thenReturn(savedOrder2);

            RegisterOrderResult result1 = registerOrderService.registerOrder(command1);
            RegisterOrderResult result2 = registerOrderService.registerOrder(command2);

            assertThat(result1.id()).isEqualTo(1L);
            assertThat(result2.id()).isEqualTo(2L);
            assertThat(result1.id()).isNotEqualTo(result2.id());
        }
    }

    // ==================================================================================
    // 엣지 케이스
    // ==================================================================================

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCaseTest {

        @Test
        @DisplayName("주문 상품이 빈 목록이면 빈 주문이 저장된다")
        void registerOrder_emptyOrderProducts_savesEmptyOrder() {
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(0L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of())
                    .build();

            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(new HashSet<>());

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).isEmpty();
        }

        @Test
        @DisplayName("totalAmount가 0이고 unitAmount도 0이면 정상 등록된다")
        void registerOrder_zeroTotalAmount_succeeds() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(0L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(0L)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, 0L);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getTotalAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("매우 큰 totalAmount도 정상 처리된다")
        void registerOrder_largeTotalAmount_succeeds() {
            Long pricePolicyId = 100L;
            Long largeTotalAmount = Long.MAX_VALUE - 1;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(largeTotalAmount)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(largeTotalAmount)
                                    .build()
                    ))
                    .build();

            mockProductPrice(pricePolicyId, largeTotalAmount);

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getTotalAmount()).isEqualTo(largeTotalAmount);
        }

        @Test
        @DisplayName("재고 조회 결과가 빈 Set인 경우 재고 검증을 건너뛴다")
        void registerOrder_emptyInventorySet_skipsValidation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);

            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(new HashSet<>());

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();
            verify(saveOrderPort).save(any(Order.class));
        }

        @Test
        @DisplayName("여러 판매자의 상품을 포함한 주문이 정상 등록된다")
        void registerOrder_multipleSellers_succeeds() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Long pricePolicyId3 = 300L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(300000L)
                    .couponAmount(10000L)
                    .pointAmount(5000L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .sharerId(1L)
                                    .quantity(2)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image1.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .sharerId(2L)
                                    .quantity(1)
                                    .unitAmount(100000L)
                                    .imageUrl("https://example.com/image2.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(300L)
                                    .sellerId(30L)
                                    .pricePolicyId(pricePolicyId3)
                                    .sharerId(null)
                                    .quantity(1)
                                    .unitAmount(100000L)
                                    .imageUrl(null)
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(50000L, 10L),
                    pricePolicyId2, Map.entry(100000L, 20L),
                    pricePolicyId3, Map.entry(100000L, 30L)
            ));
            when(modifyUserPointPort.getAvailablePoints(1L)).thenReturn(999999L);

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 50);
            Inventory inventory3 = Inventory.of(3L, pricePolicyId3, 30);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory1, inventory2, inventory3));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            Order capturedOrder = captureOrder();
            assertThat(capturedOrder.getOrderProducts()).hasSize(3);
            assertThat(capturedOrder.getOrderProducts())
                    .extracting(OrderProduct::getSellerId)
                    .containsExactly(10L, 20L, 30L);
        }
    }

    // ==================================================================================
    // 결제 배분(PaymentAllocation) 검증
    // ==================================================================================

    @Nested
    @DisplayName("결제 배분(PaymentAllocation) 검증")
    class PaymentAllocationTest {

        @Test
        @DisplayName("주문 등록 시 판매자별 PaymentAllocation이 생성된다")
        @SuppressWarnings("unchecked")
        void registerOrder_createsPaymentAllocationsPerSeller() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(25000L, 10L),
                    pricePolicyId2, Map.entry(50000L, 20L)
            ));
            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 50);
            when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                    .thenReturn(Set.of(inventory1, inventory2));
            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
            verify(savePaymentAllocationPort).saveAll(captor.capture());
            List<PaymentAllocation> allocations = captor.getValue();
            assertThat(allocations).hasSize(2);
            assertThat(allocations)
                    .extracting(PaymentAllocation::getSellerId)
                    .containsExactlyInAnyOrder(10L, 20L);
        }

        @Test
        @DisplayName("동일 판매자의 복수 상품은 금액이 합산된 단일 PaymentAllocation으로 생성된다")
        @SuppressWarnings("unchecked")
        void registerOrder_groupsAllocationsBySellerId() {
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Long sameSellerId = 10L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(150000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .productId(100L)
                                    .sellerId(sameSellerId)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(2)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .productId(200L)
                                    .sellerId(sameSellerId)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            mockProductPricesWithSellerIds(Map.of(
                    pricePolicyId1, Map.entry(50000L, sameSellerId),
                    pricePolicyId2, Map.entry(50000L, sameSellerId)
            ));
            mockInventoryMultiple(Map.of(pricePolicyId1, 100, pricePolicyId2, 50));
            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
            verify(savePaymentAllocationPort).saveAll(captor.capture());
            List<PaymentAllocation> allocations = captor.getValue();
            assertThat(allocations).hasSize(1);
            assertThat(allocations.get(0).getSellerId()).isEqualTo(sameSellerId);
            assertThat(allocations.get(0).getAllocatedAmount()).isEqualTo(150000L);
        }

        @Test
        @DisplayName("PaymentAllocation 저장 실패 시 주문 등록도 함께 실패한다 (정산 데이터 정합성 보장)")
        void registerOrder_allocationSaveFails_orderAlsoFails() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);
            doThrow(new RuntimeException("DB 저장 실패"))
                    .when(savePaymentAllocationPort).saveAll(anyList());

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB 저장 실패");
        }

        @Test
        @DisplayName("PaymentAllocation의 멱등성 키가 올바른 형식으로 설정된다")
        @SuppressWarnings("unchecked")
        void registerOrder_setsCorrectIdempotencyKey() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
            verify(savePaymentAllocationPort).saveAll(captor.capture());
            List<PaymentAllocation> allocations = captor.getValue();
            assertThat(allocations).hasSize(1);
            assertThat(allocations.get(0).getIdempotencyKey()).startsWith("ORDER_ALLOCATION:1:");
        }

        @Test
        @DisplayName("PaymentAllocation의 transactionType이 ORDER_REGISTRATION으로 설정된다")
        @SuppressWarnings("unchecked")
        void registerOrder_setsCorrectTransactionType() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 50000L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 50000L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<List<PaymentAllocation>> captor = ArgumentCaptor.forClass(List.class);
            verify(savePaymentAllocationPort).saveAll(captor.capture());
            List<PaymentAllocation> allocations = captor.getValue();
            assertThat(allocations).hasSize(1);
            assertThat(allocations.get(0).getTransactionType())
                    .isEqualTo(PaymentAllocationTransactionType.ORDER_REGISTRATION);
            assertThat(allocations.get(0).getTargetType()).isEqualTo(PaymentAllocationTargetType.ORDER);
        }

        @Test
        @DisplayName("무료 상품(단가 0원)만 있는 주문은 allocation을 생성하지 않는다")
        void registerOrder_freeProductOnly_skipsAllocation() {
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = createSingleProductCommand(1L, pricePolicyId, 0L, 1, 0L, 0L);

            mockProductPrice(pricePolicyId, 0L);
            mockInventoryAndSave(pricePolicyId, 100, 1L);

            registerOrderService.registerOrder(command);

            verifyNoInteractions(savePaymentAllocationPort);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order mockSavedOrder(Long orderId) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        when(order.getOrderKey()).thenReturn(UUID.randomUUID());
        return order;
    }

    private RegisterOrderCommand createSingleProductCommand(
            Long buyerId, Long pricePolicyId, Long unitAmount, Integer quantity,
            Long couponAmount, Long pointAmount
    ) {
        return RegisterOrderCommand.builder()
                .buyerId(buyerId)
                .totalAmount(unitAmount * quantity)
                .couponAmount(couponAmount)
                .pointAmount(pointAmount)
                .orderProducts(List.of(
                        OrderProductItemCommand.builder()
                                .productId(100L)
                                .sellerId(10L)
                                .pricePolicyId(pricePolicyId)
                                .quantity(quantity)
                                .unitAmount(unitAmount)
                                .build()
                ))
                .build();
    }

    private void mockProductPrice(Long pricePolicyId, Long price) {
        ProductInfoResult productInfo = new ProductInfoResult(
                1L, 10L, "테스트 상품", "테스트 브랜드", price, null, null, List.of()
        );
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(pricePolicyId, productInfo));
    }

    private void mockProductPriceWithSellerId(Long pricePolicyId, Long price, Long sellerId) {
        ProductInfoResult productInfo = new ProductInfoResult(
                1L, sellerId, "테스트 상품", "테스트 브랜드", price, null, null, List.of()
        );
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(pricePolicyId, productInfo));
    }

    private void mockProductPricesWithSellerIds(Map<Long, Map.Entry<Long, Long>> pricePolicyIdToPriceAndSellerId) {
        java.util.HashMap<Long, ProductInfoResult> result = new java.util.HashMap<>();
        pricePolicyIdToPriceAndSellerId.forEach((pricePolicyId, priceAndSellerId) ->
                result.put(pricePolicyId, new ProductInfoResult(
                        pricePolicyId, priceAndSellerId.getValue(), "테스트 상품", "테스트 브랜드",
                        priceAndSellerId.getKey(), null, null, List.of()
                ))
        );
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(result);
    }

    private void mockProductPrices(Map<Long, Long> pricePolicyIdToPrice) {
        java.util.HashMap<Long, ProductInfoResult> result = new java.util.HashMap<>();
        pricePolicyIdToPrice.forEach((pricePolicyId, price) ->
                result.put(pricePolicyId, new ProductInfoResult(
                        pricePolicyId, 10L, "테스트 상품", "테스트 브랜드", price, null, null, List.of()
                ))
        );
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(result);
    }

    private void mockInventoryAndSave(Long pricePolicyId, int stock, Long orderId) {
        Inventory inventory = Inventory.of(1L, pricePolicyId, stock);
        when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                .thenReturn(Set.of(inventory));

        Order savedOrder = mockSavedOrder(orderId);
        when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);
    }

    private Order captureOrder() {
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(saveOrderPort).save(captor.capture());
        return captor.getValue();
    }

    private void mockInventoryMultiple(Map<Long, Integer> pricePolicyIdToStock) {
        Set<Inventory> inventories = new HashSet<>();
        long idCounter = 1L;
        for (Map.Entry<Long, Integer> entry : pricePolicyIdToStock.entrySet()) {
            inventories.add(Inventory.of(idCounter++, entry.getKey(), entry.getValue()));
        }
        when(getInventoryUseCase.getOrCreateInventories(anyMap()))
                .thenReturn(inventories);
    }
}
