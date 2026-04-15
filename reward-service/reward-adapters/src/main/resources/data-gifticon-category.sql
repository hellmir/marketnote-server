-- 기프티콘 카테고리 초기 데이터 (기프티쇼 API 기준 7개 카테고리)
-- 실행 방법: 최초 배포 시 수동 실행 또는 application.yml sql.init.data-locations에 추가

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('1', '편의점', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('2', '외식/배달', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('3', '카페/베이커리', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('4', '아이스크림', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('5', '뷰티/패션', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('6', '생활/건강', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

INSERT INTO gifticon_category (category_code, category_name, display_name, icon_url, exposed, order_num, created_at, modified_at)
VALUES ('7', '기타', NULL, NULL, false, NULL, NOW(), NOW())
ON CONFLICT (category_code) DO NOTHING;

-- 기프티콘 카테고리 매핑 초기 데이터 (기프티쇼 category1Seq → 자체 카테고리)
-- giftishow_category_seq는 기프티쇼 API 브랜드 조회(0102) 응답의 category1Seq 값
-- gifticon_category_id는 위 gifticon_category 테이블의 id (순서대로 1~7)

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('1', 1, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('2', 2, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('3', 3, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('4', 4, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('5', 5, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('6', 6, NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO gifticon_category_mapping (giftishow_category_seq, gifticon_category_id, created_at, modified_at)
VALUES ('7', 7, NOW(), NOW())
ON CONFLICT DO NOTHING;
