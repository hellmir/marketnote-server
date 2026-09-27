SET lock_timeout = '3s';
ALTER TABLE psp_payment_event DROP COLUMN IF EXISTS user_ip;
