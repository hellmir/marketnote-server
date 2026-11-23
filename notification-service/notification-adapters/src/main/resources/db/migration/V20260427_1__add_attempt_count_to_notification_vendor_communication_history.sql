ALTER TABLE notification_vendor_communication_history
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 1;
