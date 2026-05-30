ALTER TABLE payment_allocation
    ADD COLUMN shipping_fee BIGINT NOT NULL DEFAULT 0;
