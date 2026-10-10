-- ============================================================
-- #3725 회원 기프티콘 상품 목록 cursor 기반 무한 스크롤 전환에 따른 인덱스 추가
-- ============================================================

-- 회원용 노출 리스트 조회 (필터: exposed + goods_status, 정렬: order_num, id)
CREATE INDEX IF NOT EXISTS idx_gifticon_goods_exposed_status_order
    ON gifticon_goods (exposed, goods_status, order_num, id);

-- 카테고리 필터
CREATE INDEX IF NOT EXISTS idx_gifticon_goods_category_code
    ON gifticon_goods (category_code);

-- 브랜드 필터
CREATE INDEX IF NOT EXISTS idx_gifticon_goods_brand_code
    ON gifticon_goods (brand_code);
