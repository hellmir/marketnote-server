package com.personal.marketnote.commerce.adapter.in.web.inventory.controller;

import com.personal.marketnote.commerce.adapter.in.web.inventory.request.SyncFulfillmentVendorInventoryRequest;
import com.personal.marketnote.commerce.adapter.in.web.inventory.response.GetInventoriesResponse;
import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.port.in.command.inventory.RegisterInventoryCommand;
import com.personal.marketnote.commerce.port.in.command.inventory.SyncFulfillmentVendorInventoryCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.RegisterInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.inventory.SyncFulfillmentVendorInventoryUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryController 테스트")
class InventoryControllerTest {

    @InjectMocks
    private InventoryController inventoryController;

    @Mock
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Mock
    private GetInventoryUseCase getInventoryUseCase;

    @Mock
    private SyncFulfillmentVendorInventoryUseCase syncFulfillmentVendorInventoryUseCase;

    @Nested
    @DisplayName("registerInventory")
    class RegisterInventory {

        @Test
        @DisplayName("재고 도메인 등록이 성공하면 201 CREATED를 반환한다")
        void shouldReturnCreatedWhenRegisterInventorySucceeds() {
            // given
            RegisterInventoryRequest request = new RegisterInventoryRequest(1L, 10L);

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    inventoryController.registerInventory(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerInventoryUseCase).registerInventory(any(RegisterInventoryCommand.class));
        }
    }

    @Nested
    @DisplayName("getInventories")
    class GetInventories {

        @Test
        @DisplayName("가격 정책 ID 목록으로 조회하면 200 OK와 재고 목록을 반환한다")
        void shouldReturnInventoriesByPricePolicyIds() {
            // given
            List<Long> pricePolicyIds = List.of(1L, 2L);
            Set<Inventory> inventories = Set.of(
                    Inventory.of(1L, 1L, 100),
                    Inventory.of(2L, 2L, 50)
            );
            when(getInventoryUseCase.getInventories(pricePolicyIds)).thenReturn(inventories);

            // when
            ResponseEntity<BaseResponse<GetInventoriesResponse>> response =
                    inventoryController.getInventories(pricePolicyIds, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getInventoryUseCase).getInventories(pricePolicyIds);
        }

        @Test
        @DisplayName("상품 ID 목록이 함께 제공되면 getOrCreateInventories를 호출한다")
        void shouldCallGetOrCreateWhenProductIdsProvided() {
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
            ResponseEntity<BaseResponse<GetInventoriesResponse>> response =
                    inventoryController.getInventories(pricePolicyIds, productIds);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getInventoryUseCase).getOrCreateInventories(Map.of(1L, 10L, 2L, 20L));
            verifyNoInteractions(registerInventoryUseCase);
        }
    }

    @Nested
    @DisplayName("syncFulfillmentVendorInventories")
    class SyncFulfillmentVendorInventories {

        @Test
        @DisplayName("풀필먼트 벤더 재고 동기화가 성공하면 200 OK를 반환한다")
        void shouldReturnOkWhenSyncSucceeds() {
            // given
            SyncFulfillmentVendorInventoryRequest request = mock(SyncFulfillmentVendorInventoryRequest.class);
            when(request.getInventories()).thenReturn(List.of());

            // when
            ResponseEntity<BaseResponse<Void>> response =
                    inventoryController.syncFulfillmentVendorInventories(request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(syncFulfillmentVendorInventoryUseCase)
                    .syncInventories(any(SyncFulfillmentVendorInventoryCommand.class));
        }
    }
}
