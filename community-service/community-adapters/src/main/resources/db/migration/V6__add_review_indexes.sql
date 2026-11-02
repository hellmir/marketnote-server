-- ============================================================
-- review 테이블 조회 성능 최적화 인덱스
-- 1. (product_id, status, id): 상품 리뷰 목록 커서 페이징 / count
-- 2. (reviewer_id, status, id): 유저 리뷰 목록 커서 페이징 / count
-- 3. UNIQUE (order_id, price_policy_id): 동일 주문-가격정책 중복 리뷰 방지 + existsBy 최적화
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_review_product_id_status_id ON review (product_id, status, id);

CREATE INDEX IF NOT EXISTS idx_review_reviewer_id_status_id ON review (reviewer_id, status, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_review_order_id_price_policy_id ON review (order_id, price_policy_id);
