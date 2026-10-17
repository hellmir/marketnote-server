-- ============================================================
-- order_product_read_models (BaseGeneralEntity)
-- 구매 단가 ReadModel: ORDER_PAYMENT_COMPLETED 이벤트 Consumer가 적재
-- ============================================================
CREATE TABLE order_product_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    price_policy_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    unit_amount BIGINT NOT NULL,
    CONSTRAINT uk_order_product_read_model_order_price_policy
        UNIQUE (order_id, price_policy_id)
);
