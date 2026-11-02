-- #4085: 풀필먼트 goods_registration productKey 기반 전환 (1단계) — product_key 컬럼 추가
ALTER TABLE fassto_goods_registration
    ADD COLUMN product_key UUID;

ALTER TABLE fassto_goods_registration
    ADD CONSTRAINT uk_fassto_goods_registration_product_key UNIQUE (product_key);
