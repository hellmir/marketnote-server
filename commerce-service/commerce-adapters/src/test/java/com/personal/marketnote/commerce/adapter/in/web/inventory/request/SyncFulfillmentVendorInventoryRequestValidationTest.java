package com.personal.marketnote.commerce.adapter.in.web.inventory.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SyncFulfillmentVendorInventoryRequest 유효성 검증 테스트")
class SyncFulfillmentVendorInventoryRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("inventories 개수가 1000개 이하이면 크기 검증을 통과한다")
    void shouldPassSizeValidationWhenInventoriesCountWithinLimit() {
        SyncFulfillmentVendorInventoryRequest request = buildRequest(1000);

        Set<ConstraintViolation<SyncFulfillmentVendorInventoryRequest>> violations = validator.validate(request);

        assertThat(violations).noneMatch(v ->
                v.getPropertyPath().toString().equals("inventories")
                        && v.getMessage().contains("1000")
        );
    }

    @Test
    @DisplayName("inventories 개수가 1001개이면 크기 검증에 실패한다")
    void shouldFailSizeValidationWhenInventoriesCountExceedsLimit() {
        SyncFulfillmentVendorInventoryRequest request = buildRequest(1001);

        Set<ConstraintViolation<SyncFulfillmentVendorInventoryRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("inventories")
                        && v.getMessage().contains("1000")
        );
    }

    @Test
    @DisplayName("inventories가 빈 리스트이면 NotEmpty 검증에 실패한다")
    void shouldFailWhenInventoriesIsEmpty() {
        SyncFulfillmentVendorInventoryRequest request = buildRequest(0);

        Set<ConstraintViolation<SyncFulfillmentVendorInventoryRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("inventories")
                        && v.getMessage().contains("필수값")
        );
    }

    private SyncFulfillmentVendorInventoryRequest buildRequest(int itemCount) {
        SyncFulfillmentVendorInventoryRequest request = new SyncFulfillmentVendorInventoryRequest();
        List<SyncFulfillmentVendorInventoryItemRequest> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            SyncFulfillmentVendorInventoryItemRequest item = new SyncFulfillmentVendorInventoryItemRequest();
            setField(item, "productKey", UUID.randomUUID());
            setField(item, "stock", 100);
            items.add(item);
        }
        setField(request, "inventories", items);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("테스트 필드 주입 실패: " + fieldName, e);
        }
    }
}
