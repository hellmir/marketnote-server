ALTER TABLE shipping_policy_read_models
    ADD COLUMN jeju_surcharge BIGINT DEFAULT 0;
ALTER TABLE shipping_policy_read_models
    ADD COLUMN island_surcharge BIGINT DEFAULT 0;
