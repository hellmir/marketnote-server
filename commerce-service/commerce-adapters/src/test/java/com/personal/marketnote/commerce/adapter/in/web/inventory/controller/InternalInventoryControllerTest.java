package com.personal.marketnote.commerce.adapter.in.web.inventory.controller;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.common.adapter.in.request.RegisterInventoryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalInventoryController 테스트")
class InternalInventoryControllerTest {
    @InjectMocks
    private InternalInventoryController internalInventoryController;

    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Mock
    private GetInventoryUseCase getInventoryUseCase;

    @Nested
    @DisplayName("registerInventory")
    class RegisterInventory {
        @Test
        @DisplayName("재고 도메인 등록 요청이 성공하면 201 CREATED를 반환한다")
        void shouldReturnCreatedWhenRegisterInventorySucceeds() {
            // given
            RegisterInventoryRequest request = new RegisterInventoryRequest(1L, 100L);

            // when
            ResponseEntity<?> response = internalInventoryController.registerInventory(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerInventoryUseCase).registerInventory(any());
        }
    }

    @Nested
    @DisplayName("getInventories")
    class GetInventories {
        @Test
        @DisplayName("가격정책 ID 목록으로 재고를 조회하면 200 OK를 반환한다")
        void shouldReturnOkWhenGetInventoriesSucceeds() {
            // given
            List<Long> pricePolicyIds = List.of(1L, 2L);
            Set<Inventory> inventories = Set.of(
                    Inventory.of(1L, 1L, 100),
                    Inventory.of(2L, 2L, 50)
            );
            when(getInventoryUseCase.getInventories(pricePolicyIds)).thenReturn(inventories);

            // when
            ResponseEntity<?> response = internalInventoryController.getInventories(pricePolicyIds, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getInventoryUseCase).getInventories(pricePolicyIds);
            verifyNoInteractions(registerInventoryUseCase);
        }

        @Test
        @DisplayName("가격정책 ID와 상품 ID 목록으로 재고를 조회하면 getOrCreateInventories를 호출한다")
        void shouldCallGetOrCreateInventoriesWhenProductIdsProvided() {
            // given
            List<Long> pricePolicyIds = List.of(1L, 2L);
            List<Long> productIds = List.of(10L, 20L);
            Set<Inventory> inventories = Set.of(
                    Inventory.of(10L, 1L, 100),
                    Inventory.of(20L, 2L, 50)
            );
            when(getInventoryUseCase.getOrCreateInventories(Map.of(1L, 10L, 2L, 20L)))
                    .thenReturn(inventories);

            // when
            ResponseEntity<?> response = internalInventoryController.getInventories(pricePolicyIds, productIds);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getInventoryUseCase).getOrCreateInventories(Map.of(1L, 10L, 2L, 20L));
            verifyNoMoreInteractions(getInventoryUseCase);
        }
    }
}
