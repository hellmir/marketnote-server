-- 알리고 메시지 대체문자(failover) 필드 추가
ALTER TABLE aligo_message ADD COLUMN failover_used BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE aligo_message ADD COLUMN failover_subject VARCHAR(100);
ALTER TABLE aligo_message ADD COLUMN failover_message_body TEXT;

-- 알리고 메시지 수신자 대체문자 mid 필드 추가
ALTER TABLE aligo_message_recipient ADD COLUMN smid VARCHAR(100);
