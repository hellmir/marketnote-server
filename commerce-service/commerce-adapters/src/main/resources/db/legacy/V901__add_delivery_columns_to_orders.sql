ALTER TABLE orders
    ADD COLUMN delivery_recipient_name VARCHAR(50);
ALTER TABLE orders
    ADD COLUMN delivery_recipient_phone_number VARCHAR(20);
ALTER TABLE orders
    ADD COLUMN delivery_zip_code VARCHAR(10);
ALTER TABLE orders
    ADD COLUMN delivery_address VARCHAR(255);
ALTER TABLE orders
    ADD COLUMN delivery_address_detail VARCHAR(255);
ALTER TABLE orders
    ADD COLUMN delivery_request_message VARCHAR(100);
