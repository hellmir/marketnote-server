-- pgcrypto: UUID 생성 함수 보장
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- orders (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    buyer_id BIGINT NOT NULL,
    order_key UUID NOT NULL,
    order_number VARCHAR(31) NOT NULL,
    order_status VARCHAR(31) NOT NULL,
    total_amount BIGINT NOT NULL,
    paid_amount BIGINT,
    coupon_amount BIGINT,
    point_amount BIGINT,
    shipping_fee BIGINT,
    delivery_recipient_name VARCHAR(50),
    delivery_recipient_phone_number VARCHAR(20),
    delivery_zip_code VARCHAR(10),
    delivery_address VARCHAR(255),
    delivery_address_detail VARCHAR(255),
    delivery_request_type VARCHAR(31),
    delivery_request_message VARCHAR(60),
    pickup_recipient_name VARCHAR(50),
    pickup_recipient_phone_number VARCHAR(20),
    pickup_zip_code VARCHAR(10),
    pickup_address VARCHAR(255),
    pickup_address_detail VARCHAR(255),
    pickup_delivery_request_type VARCHAR(31),
    pickup_request_message VARCHAR(60),
    CONSTRAINT uk_orders_order_key UNIQUE (order_key),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number)
);

CREATE INDEX IF NOT EXISTS idx_orders_buyer_id_created_at ON orders (buyer_id, created_at);

-- ============================================================
-- order_product (BaseEntity, EmbeddedId)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_product (
    price_policy_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    seller_id BIGINT NOT NULL,
    order_product_key UUID NOT NULL,
    sharer_key UUID,
    quantity INTEGER NOT NULL,
    unit_amount BIGINT NOT NULL,
    image_url VARCHAR(511),
    accumulated_point BIGINT,
    order_status VARCHAR(31) NOT NULL,
    review_yn BOOLEAN DEFAULT FALSE NOT NULL,
    confirmed_at TIMESTAMP(6),
    delivered_at TIMESTAMP(6),
    PRIMARY KEY (price_policy_id, order_id),
    CONSTRAINT uk_order_product_order_product_key UNIQUE (order_product_key),
    CONSTRAINT fk_order_product_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- ============================================================
-- order_status_history (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    order_status VARCHAR(31) NOT NULL,
    reason_category VARCHAR(31) NOT NULL,
    reason VARCHAR(63) NOT NULL,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- ============================================================
-- payment (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    order_key UUID NOT NULL,
    pg_payment_key VARCHAR(255),
    payment_amount BIGINT NOT NULL,
    success_yn BOOLEAN,
    refunded_yn BOOLEAN NOT NULL,
    refund_amount BIGINT,
    version BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT uk_payment_order_key UNIQUE (order_key)
);

CREATE INDEX IF NOT EXISTS idx_payment_order_id ON payment (order_id);

-- ============================================================
-- psp_payment_event (BaseEntity, partial unique index)
-- ============================================================
CREATE TABLE IF NOT EXISTS psp_payment_event (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    order_key VARCHAR(63) NOT NULL,
    pg_company_key VARCHAR(255) NOT NULL,
    pg_shop_key VARCHAR(255),
    pg_payment_key VARCHAR(255),
    pg_approval_result VARCHAR(1023),
    pg_cancel_approval_result VARCHAR(1023),
    shop_transaction_id VARCHAR(255),
    po_status VARCHAR(15) NOT NULL,
    method VARCHAR(15) NOT NULL,
    amount BIGINT NOT NULL,
    vat_amount BIGINT,
    nat_amount BIGINT,
    card_number VARCHAR(511),
    approval_number VARCHAR(511),
    installment SMALLINT,
    issue_company_code VARCHAR(255),
    issue_company_name VARCHAR(255),
    purchase_company_code VARCHAR(255),
    purchase_company_name VARCHAR(255),
    bank_code VARCHAR(255),
    bank_name VARCHAR(255),
    bill_number VARCHAR(255),
    bill_sequence_number VARCHAR(255),
    user_ip VARCHAR(31),
    payment_info VARCHAR(2047),
    payment_source VARCHAR(127),
    result_code VARCHAR(255),
    result_message VARCHAR(511),
    paid_at TIMESTAMP(6),
    version BIGINT DEFAULT 0 NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_psp_payment_event_order_key ON psp_payment_event (order_key);
CREATE UNIQUE INDEX IF NOT EXISTS idx_psp_payment_event_order_key_active
    ON psp_payment_event (order_key)
    WHERE po_status IN ('READY', 'EXECUTING', 'UNKNOWN');

-- ============================================================
-- refund (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS refund (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    payment_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    refund_amount BIGINT NOT NULL,
    cancel_reason VARCHAR(500),
    processed_by VARCHAR(100),
    pg_refund_key VARCHAR(200),
    pg_raw_response TEXT
);

CREATE INDEX IF NOT EXISTS idx_refund_order_id ON refund (order_id);
CREATE INDEX IF NOT EXISTS idx_refund_payment_id ON refund (payment_id);

-- ============================================================
-- account (BaseEntity, seed 데이터 포함)
-- ============================================================
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    name VARCHAR(31) NOT NULL,
    account_type VARCHAR(15) NOT NULL,
    status VARCHAR(15) NOT NULL,
    CONSTRAINT uk_account_name UNIQUE (name)
);

INSERT INTO account (name, account_type, status, created_at) VALUES
    ('매출채권_PG', 'ASSET', 'ACTIVE', NOW()),
    ('보통예금', 'ASSET', 'ACTIVE', NOW()),
    ('미지급금_판매자', 'LIABILITY', 'ACTIVE', NOW()),
    ('플랫폼수수료수익', 'REVENUE', 'ACTIVE', NOW()),
    ('PG수수료비용', 'EXPENSE', 'ACTIVE', NOW())
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- ledger_transaction (BaseEntity 미상속, @CreatedDate only)
-- ============================================================
CREATE TABLE IF NOT EXISTS ledger_transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(31) NOT NULL,
    target_type VARCHAR(15) NOT NULL,
    target_id BIGINT,
    description VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_ledger_transaction_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_ledger_transaction_target ON ledger_transaction (target_type, target_id);

-- ============================================================
-- ledger_entry (BaseEntity 미상속, @CreatedDate only, CHECK)
-- ============================================================
CREATE TABLE IF NOT EXISTS ledger_entry (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    transaction_type VARCHAR(15) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT ck_ledger_entry_amount_positive CHECK (amount > 0),
    CONSTRAINT fk_ledger_entry_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_ledger_entry_transaction FOREIGN KEY (transaction_id) REFERENCES ledger_transaction (id)
);

CREATE INDEX IF NOT EXISTS idx_ledger_entry_account_id ON ledger_entry (account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_transaction_id ON ledger_entry (transaction_id);

-- ============================================================
-- settlement (BaseEntity 미상속, CHECK)
-- ============================================================
CREATE TABLE IF NOT EXISTS settlement (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    total_allocated_amount BIGINT NOT NULL,
    shipping_fee BIGINT NOT NULL,
    pg_fee_amount BIGINT NOT NULL,
    platform_fee_amount BIGINT NOT NULL,
    seller_payout_amount BIGINT NOT NULL,
    status VARCHAR(15) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    CONSTRAINT idx_settlement_seller_year_month UNIQUE (seller_id, year, month),
    CONSTRAINT ck_settlement_total_allocated_amount_positive CHECK (total_allocated_amount > 0),
    CONSTRAINT ck_settlement_seller_payout_amount_positive CHECK (seller_payout_amount > 0)
);

CREATE INDEX IF NOT EXISTS idx_settlement_year_month ON settlement (year, month);
CREATE INDEX IF NOT EXISTS idx_settlement_status ON settlement (status);

-- ============================================================
-- payment_allocation (BaseEntity 미상속, @CreatedDate only, CHECK)
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_allocation (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    allocated_amount BIGINT NOT NULL,
    shipping_fee BIGINT NOT NULL,
    settlement_id BIGINT,
    transaction_type VARCHAR(31) NOT NULL,
    target_type VARCHAR(15) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_payment_allocation_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT ck_payment_allocation_allocated_amount_positive CHECK (allocated_amount > 0),
    CONSTRAINT fk_payment_allocation_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_payment_allocation_settlement FOREIGN KEY (settlement_id) REFERENCES settlement (id)
);

CREATE INDEX IF NOT EXISTS idx_payment_allocation_created_at_settlement_id
    ON payment_allocation (created_at, settlement_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_order_id ON payment_allocation (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_seller_id ON payment_allocation (seller_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_settlement_id ON payment_allocation (settlement_id);
CREATE INDEX IF NOT EXISTS idx_payment_allocation_seller_unsettled
    ON payment_allocation (seller_id)
    WHERE settlement_id IS NULL;

-- ============================================================
-- settlement_policy (BaseEntity 미상속, partial unique + CHECK)
-- ============================================================
CREATE TABLE IF NOT EXISTS settlement_policy (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    pg_fee_rate INTEGER NOT NULL,
    platform_fee_rate INTEGER NOT NULL,
    settlement_cycle VARCHAR(20) NOT NULL,
    min_payout_amount BIGINT NOT NULL,
    status VARCHAR(15) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    CONSTRAINT ck_settlement_policy_pg_fee_rate CHECK (pg_fee_rate BETWEEN 0 AND 10000),
    CONSTRAINT ck_settlement_policy_platform_fee_rate CHECK (platform_fee_rate BETWEEN 0 AND 10000),
    CONSTRAINT ck_settlement_policy_min_payout_amount CHECK (min_payout_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_settlement_policy_seller_id ON settlement_policy (seller_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_settlement_policy_seller_active
    ON settlement_policy (seller_id)
    WHERE status = 'ACTIVE';

-- ============================================================
-- inventory (BaseEntity, PK=price_policy_id 외부 주입)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    price_policy_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    product_id BIGINT,
    stock INT DEFAULT 0 NOT NULL,
    reserved INT DEFAULT 0 NOT NULL,
    version BIGINT DEFAULT 0 NOT NULL
);

-- ============================================================
-- inventory_addition_history (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_addition_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    product_id BIGINT,
    price_policy_id BIGINT NOT NULL,
    stock INTEGER NOT NULL,
    reason VARCHAR(511),
    unit_price BIGINT,
    supplier VARCHAR(255)
);

-- ============================================================
-- inventory_deduction_history (BaseEntity, partial unique)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_deduction_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    product_id BIGINT,
    price_policy_id BIGINT NOT NULL,
    order_id BIGINT,
    stock INTEGER NOT NULL,
    reason VARCHAR(511)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_deduction_order_policy
    ON inventory_deduction_history (order_id, price_policy_id)
    WHERE order_id IS NOT NULL;

-- ============================================================
-- inventory_restoration_history (BaseEntity, partial unique)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_restoration_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    product_id BIGINT,
    price_policy_id BIGINT NOT NULL,
    order_id BIGINT,
    stock INTEGER NOT NULL,
    reason VARCHAR(511)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_restoration_order_policy
    ON inventory_restoration_history (order_id, price_policy_id)
    WHERE order_id IS NOT NULL;

-- ============================================================
-- inventory_reservation (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_reservation (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    price_policy_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    reserved_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_inventory_reservation_order_policy UNIQUE (order_id, price_policy_id)
);

-- ============================================================
-- shop_mapper (BaseEntity, PK=price_policy_id)
-- ============================================================
CREATE TABLE IF NOT EXISTS shop_mapper (
    price_policy_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    wms_key VARCHAR(255) DEFAULT 'true' NOT NULL,
    wms_product_key VARCHAR(255),
    stock INTEGER NOT NULL
);

-- ============================================================
-- quick_payment_card (BaseEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS quick_payment_card (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    batch_key VARCHAR(255) NOT NULL,
    group_id VARCHAR(12),
    card_code VARCHAR(4) NOT NULL,
    card_name VARCHAR(20) NOT NULL,
    masked_card_number VARCHAR(20),
    card_bin_type_01 VARCHAR(1) NOT NULL,
    card_bin_type_02 VARCHAR(1) NOT NULL,
    status VARCHAR(15) NOT NULL,
    CONSTRAINT uk_quick_payment_card_batch_key UNIQUE (batch_key)
);

CREATE INDEX IF NOT EXISTS idx_quick_payment_card_user_id ON quick_payment_card (user_id);

-- ============================================================
-- return_tracker (BaseEntity 미상속, @CreatedDate/@LastModifiedDate)
-- ============================================================
CREATE TABLE IF NOT EXISTS return_tracker (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    return_slip_number VARCHAR(100),
    inspection_status VARCHAR(20) NOT NULL,
    refund_status VARCHAR(20) NOT NULL,
    inspected_at TIMESTAMP(6),
    refunded_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    CONSTRAINT uk_return_tracker_order_id UNIQUE (order_id)
);

CREATE INDEX IF NOT EXISTS idx_return_tracker_inspection_status ON return_tracker (inspection_status);

-- ============================================================
-- product_read_models (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    price_policy_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    price BIGINT,
    discount_price BIGINT,
    accumulated_point BIGINT,
    CONSTRAINT uk_product_read_model_price_policy_id UNIQUE (price_policy_id)
);

-- ============================================================
-- shipping_policy_read_models (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS shipping_policy_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    seller_id BIGINT NOT NULL,
    shipping_fee BIGINT NOT NULL,
    free_shipping_threshold BIGINT NOT NULL,
    jeju_surcharge BIGINT,
    island_surcharge BIGINT,
    CONSTRAINT uk_shipping_policy_read_model_seller_id UNIQUE (seller_id)
);

-- ============================================================
-- shipping_address_read_models (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS shipping_address_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    shipping_address_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(31) NOT NULL,
    recipient_phone_number VARCHAR(15) NOT NULL,
    address VARCHAR(255) NOT NULL,
    address_detail VARCHAR(255) NOT NULL,
    region_type VARCHAR(20),
    CONSTRAINT uk_shipping_address_read_model_shipping_address_id UNIQUE (shipping_address_id)
);

CREATE INDEX IF NOT EXISTS idx_shipping_address_read_model_user_id ON shipping_address_read_models (user_id);

-- ============================================================
-- fulfillment_work_status_read_models (BaseGeneralEntity)
-- ============================================================
CREATE TABLE IF NOT EXISTS fulfillment_work_status_read_models (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    order_id BIGINT NOT NULL,
    work_status VARCHAR(255) NOT NULL,
    CONSTRAINT uk_fulfillment_work_status_read_model_order_id UNIQUE (order_id)
);

-- ============================================================
-- commerce_vendor_communication_history (BaseEntity 미상속, @CreatedDate only)
-- ============================================================
CREATE TABLE IF NOT EXISTS commerce_vendor_communication_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(31) NOT NULL,
    target_id VARCHAR(63),
    vendor_name VARCHAR(31) NOT NULL,
    communication_type VARCHAR(15) NOT NULL,
    sender VARCHAR(15) NOT NULL,
    exception VARCHAR(63),
    payload TEXT,
    payload_json JSONB,
    created_at TIMESTAMP(6) NOT NULL
);

-- ============================================================
-- commerce_service_communication_failure_history (BaseEntity 미상속, @CreatedDate only)
-- ============================================================
CREATE TABLE IF NOT EXISTS commerce_service_communication_failure_history (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(31) NOT NULL,
    target_id VARCHAR(63),
    communication_type VARCHAR(15) NOT NULL,
    sender VARCHAR(15) NOT NULL,
    exception VARCHAR(63),
    payload TEXT,
    payload_json JSONB,
    created_at TIMESTAMP(6) NOT NULL
);

-- ============================================================
-- common: outbox_event
-- ============================================================
CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INTEGER NOT NULL,
    max_retries INTEGER NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    published_at TIMESTAMP(6),
    failed_at TIMESTAMP(6),
    last_error_message VARCHAR(1000),
    discard_reason VARCHAR(500),
    discarded_at TIMESTAMP(6),
    CONSTRAINT uk_outbox_event_id UNIQUE (event_id)
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created ON outbox_event (status, created_at);

-- ============================================================
-- common: saga_instance
-- ============================================================
CREATE TABLE IF NOT EXISTS saga_instance (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(100) NOT NULL,
    saga_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step_index INTEGER NOT NULL,
    payload TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    CONSTRAINT uk_saga_instance_saga_id UNIQUE (saga_id)
);

CREATE INDEX IF NOT EXISTS idx_saga_instance_type_status ON saga_instance (saga_type, status);
CREATE INDEX IF NOT EXISTS idx_saga_instance_status_modified_at ON saga_instance (status, modified_at);

-- ============================================================
-- common: saga_step
-- ============================================================
CREATE TABLE IF NOT EXISTS saga_step (
    id BIGSERIAL PRIMARY KEY,
    saga_instance_id BIGINT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    step_index INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    request TEXT NOT NULL,
    response TEXT,
    compensation_request TEXT,
    compensation_response TEXT,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6)
);

CREATE INDEX IF NOT EXISTS idx_saga_step_instance_id_index ON saga_step (saga_instance_id, step_index);

-- ============================================================
-- common: dlt_message_resolution
-- ============================================================
CREATE TABLE IF NOT EXISTS dlt_message_resolution (
    id BIGSERIAL PRIMARY KEY,
    original_topic VARCHAR(100) NOT NULL,
    dlt_topic VARCHAR(100) NOT NULL,
    partition_number INTEGER NOT NULL,
    offset_number BIGINT NOT NULL,
    resolution VARCHAR(20) NOT NULL,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMP(6),
    reason VARCHAR(500),
    CONSTRAINT uk_dlt_resolution_topic_partition_offset UNIQUE (dlt_topic, partition_number, offset_number)
);

CREATE INDEX IF NOT EXISTS idx_dlt_resolution_original_topic ON dlt_message_resolution (original_topic);
