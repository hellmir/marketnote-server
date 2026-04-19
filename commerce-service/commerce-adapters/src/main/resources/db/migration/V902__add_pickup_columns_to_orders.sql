ALTER TABLE orders
    ADD COLUMN pickup_recipient_name VARCHAR(50);
ALTER TABLE orders
    ADD COLUMN pickup_recipient_phone_number VARCHAR(20);
ALTER TABLE orders
    ADD COLUMN pickup_zip_code VARCHAR(10);
ALTER TABLE orders
    ADD COLUMN pickup_address VARCHAR(255);
ALTER TABLE orders
    ADD COLUMN pickup_address_detail VARCHAR(255);
ALTER TABLE orders
    ADD COLUMN pickup_request_message VARCHAR(60);
