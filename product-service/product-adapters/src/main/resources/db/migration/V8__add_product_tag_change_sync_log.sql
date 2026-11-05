-- ============================================================
-- product_tag_change_sync_log (#4041)
-- ProductTagChangedEvent 소비 시 중복 수신 방어용 멱등 로그.
-- (event_id) 유니크 제약으로 동일 이벤트 재처리를 차단한다.
-- ============================================================
CREATE TABLE product_tag_change_sync_log (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    event_id UUID NOT NULL,
    CONSTRAINT uk_product_tag_change_sync_log_event_id UNIQUE (event_id)
);
