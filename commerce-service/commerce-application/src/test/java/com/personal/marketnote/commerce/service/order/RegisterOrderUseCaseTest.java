package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InsufficientQuantityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterOrderUseCaseTest {
    @Mock
    private GetInventoryUseCase getInventoryUseCase;
    @Mock
    private SaveOrderPort saveOrderPort;

    @InjectMocks
    private RegisterOrderService registerOrderService;

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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(5L)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .imageUrl("https://example.com/image.png")
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .sharerId(5L)
                                    .quantity(2)
                                    .unitAmount(25000L)
                                    .imageUrl("https://example.com/image1.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .sharerId(6L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image2.png")
                                    .build()
                    ))
                    .build();

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 50);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId1, pricePolicyId2)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(null)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image.png")
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(5L)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl(null)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getBuyerId()).isEqualTo(buyerId);
        }

        @Test
        @DisplayName("주문 등록 시 totalAmount가 정확하게 저장된다")
        void registerOrder_savesTotalAmountCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long totalAmount = 150000L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(totalAmount)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(150000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getTotalAmount()).isEqualTo(totalAmount);
        }

        @Test
        @DisplayName("주문 등록 시 초기 상태가 PAYMENT_PENDING으로 설정된다")
        void registerOrder_setsInitialStatusToPaymentPending() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("주문 등록 시 orderKey가 자동 생성된다")
        void registerOrder_generatesOrderKey() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderKey()).isNotNull();
        }

        @Test
        @DisplayName("주문 등록 시 orderNumber가 자동 생성된다")
        void registerOrder_generatesOrderNumber() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

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
                                    .sellerId(sellerId)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getSellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("주문 상품의 pricePolicyId가 정확하게 저장된다")
        void registerOrder_savesPricePolicyIdCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 777L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getPricePolicyId()).isEqualTo(pricePolicyId);
        }

        @Test
        @DisplayName("주문 상품의 quantity가 정확하게 저장된다")
        void registerOrder_savesQuantityCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Integer quantity = 5;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(250000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(quantity)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("주문 상품의 unitAmount가 정확하게 저장된다")
        void registerOrder_savesUnitAmountCorrectly() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long unitAmount = 75000L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(75000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(unitAmount)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .imageUrl(imageUrl)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

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
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .sharerId(sharerId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getSharerId()).isEqualTo(sharerId);
        }

        @Test
        @DisplayName("주문 상품의 초기 상태가 PAYMENT_PENDING으로 설정된다")
        void registerOrder_setsOrderProductStatusToPaymentPending() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(1);
            assertThat(capturedOrder.getOrderProducts().get(0).getOrderStatus())
                    .isEqualTo(OrderStatus.PAYMENT_PENDING);
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
            Long buyerId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId1, pricePolicyId2)))
                    .thenReturn(Set.of(inventory1, inventory2));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            verify(getInventoryUseCase).getInventories(List.of(pricePolicyId1, pricePolicyId2));
        }

        @Test
        @DisplayName("재고가 충분하면 주문이 정상 처리된다")
        void registerOrder_sufficientStock_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(250000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(5)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 10);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            int orderQuantity = 10;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(orderQuantity)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, orderQuantity);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(10)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 5);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class)
                    .hasMessageContaining("재고 수량이 부족합니다");

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("재고가 0이고 주문 수량이 1 이상이면 InsufficientQuantityException이 발생한다")
        void registerOrder_zeroStock_throwsException() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 0);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("복수 상품 중 하나라도 재고가 부족하면 InsufficientQuantityException이 발생한다")
        void registerOrder_oneProductInsufficientStock_throwsException() {
            Long buyerId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .quantity(5)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .quantity(10)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 5);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId1, pricePolicyId2)))
                    .thenReturn(Set.of(inventory1, inventory2));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(saveOrderPort, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("동일 pricePolicyId의 복수 상품이 있으면 수량을 합산하여 재고 검증한다")
        void registerOrder_samePricePolicyMultipleItems_sumsQuantityForValidation() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(150000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(3)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(4)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 6);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId, pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class)
                    .hasMessageContaining("현재 재고 수량: 6")
                    .hasMessageContaining("주문 수량: 7");
        }

        @Test
        @DisplayName("동일 pricePolicyId의 복수 상품이 있고 합산 재고가 충분하면 성공한다")
        void registerOrder_samePricePolicyMultipleItems_sufficientStock_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(150000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(3)
                                    .unitAmount(50000L)
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(4)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 10);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId, pricePolicyId)))
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
        @DisplayName("주문 등록 시 재고 조회 -> 주문 저장 순서로 호출한다")
        void registerOrder_callsInOrder() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            registerOrderService.registerOrder(command);

            var inOrder = inOrder(getInventoryUseCase, saveOrderPort);
            inOrder.verify(getInventoryUseCase).getInventories(List.of(pricePolicyId));
            inOrder.verify(saveOrderPort).save(any(Order.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("재고 검증 실패 시 SaveOrderPort가 호출되지 않는다")
        void registerOrder_stockValidationFails_doesNotCallSaveOrderPort() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(500000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(10)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 5);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isInstanceOf(InsufficientQuantityException.class);

            verify(getInventoryUseCase).getInventories(List.of(pricePolicyId));
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
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            RuntimeException exception = new RuntimeException("재고 조회 실패");
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenThrow(exception);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isSameAs(exception);

            verifyNoInteractions(saveOrderPort);
        }

        @Test
        @DisplayName("주문 저장 중 예외 발생 시 예외를 전파한다")
        void registerOrder_saveOrderFails_propagates() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            RuntimeException exception = new RuntimeException("주문 저장 실패");
            when(saveOrderPort.save(any(Order.class))).thenThrow(exception);

            assertThatThrownBy(() -> registerOrderService.registerOrder(command))
                    .isSameAs(exception);
        }

        @Test
        @DisplayName("재고 조회 실패 시 재고 검증이 수행되지 않는다")
        void registerOrder_getInventoriesFails_noValidation() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long expectedOrderId = 999L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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

            RegisterOrderCommand command1 = RegisterOrderCommand.builder()
                    .buyerId(1L)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            RegisterOrderCommand command2 = RegisterOrderCommand.builder()
                    .buyerId(2L)
                    .totalAmount(100000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(2)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(anyList()))
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
            Long buyerId = 1L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(0L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of())
                    .build();

            when(getInventoryUseCase.getInventories(List.of()))
                    .thenReturn(new HashSet<>());

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).isEmpty();
        }

        @Test
        @DisplayName("totalAmount가 0인 주문도 정상 등록된다")
        void registerOrder_zeroTotalAmount_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(0L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(0L)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getTotalAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("매우 큰 totalAmount도 정상 처리된다")
        void registerOrder_largeTotalAmount_succeeds() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            Long largeTotalAmount = Long.MAX_VALUE - 1;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(largeTotalAmount)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(largeTotalAmount)
                                    .build()
                    ))
                    .build();

            Inventory inventory = Inventory.of(1L, pricePolicyId, 100);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
                    .thenReturn(Set.of(inventory));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getTotalAmount()).isEqualTo(largeTotalAmount);
        }

        @Test
        @DisplayName("재고 조회 결과가 빈 Set인 경우 재고 검증을 건너뛴다")
        void registerOrder_emptyInventorySet_skipsValidation() {
            Long buyerId = 1L;
            Long pricePolicyId = 100L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(50000L)
                    .couponAmount(0L)
                    .pointAmount(0L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId)
                                    .quantity(1)
                                    .unitAmount(50000L)
                                    .build()
                    ))
                    .build();

            when(getInventoryUseCase.getInventories(List.of(pricePolicyId)))
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
            Long buyerId = 1L;
            Long pricePolicyId1 = 100L;
            Long pricePolicyId2 = 200L;
            Long pricePolicyId3 = 300L;
            RegisterOrderCommand command = RegisterOrderCommand.builder()
                    .buyerId(buyerId)
                    .totalAmount(300000L)
                    .couponAmount(10000L)
                    .pointAmount(5000L)
                    .orderProducts(List.of(
                            OrderProductItemCommand.builder()
                                    .sellerId(10L)
                                    .pricePolicyId(pricePolicyId1)
                                    .sharerId(1L)
                                    .quantity(2)
                                    .unitAmount(50000L)
                                    .imageUrl("https://example.com/image1.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(20L)
                                    .pricePolicyId(pricePolicyId2)
                                    .sharerId(2L)
                                    .quantity(1)
                                    .unitAmount(100000L)
                                    .imageUrl("https://example.com/image2.png")
                                    .build(),
                            OrderProductItemCommand.builder()
                                    .sellerId(30L)
                                    .pricePolicyId(pricePolicyId3)
                                    .sharerId(null)
                                    .quantity(1)
                                    .unitAmount(100000L)
                                    .imageUrl(null)
                                    .build()
                    ))
                    .build();

            Inventory inventory1 = Inventory.of(1L, pricePolicyId1, 100);
            Inventory inventory2 = Inventory.of(2L, pricePolicyId2, 50);
            Inventory inventory3 = Inventory.of(3L, pricePolicyId3, 30);
            when(getInventoryUseCase.getInventories(List.of(pricePolicyId1, pricePolicyId2, pricePolicyId3)))
                    .thenReturn(Set.of(inventory1, inventory2, inventory3));

            Order savedOrder = mockSavedOrder(1L);
            when(saveOrderPort.save(any(Order.class))).thenReturn(savedOrder);

            RegisterOrderResult result = registerOrderService.registerOrder(command);

            assertThat(result).isNotNull();

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(saveOrderPort).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getOrderProducts()).hasSize(3);
            assertThat(capturedOrder.getOrderProducts())
                    .extracting(OrderProduct::getSellerId)
                    .containsExactly(10L, 20L, 30L);
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private Order mockSavedOrder(Long orderId) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(orderId);
        return order;
    }
}
