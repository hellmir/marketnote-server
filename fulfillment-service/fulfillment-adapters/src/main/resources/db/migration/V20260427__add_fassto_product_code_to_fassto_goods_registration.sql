-- #5187: SyncFulfillmentStockService 재고 동기화 회귀 fix
-- Fastto 응답의 customerProductCode(= cstGodCd)를 productKey 로 역매핑하기 위해
-- fassto_goods_registration 테이블에 fassto_product_code 컬럼을 추가한다.
-- 신규 row는 ProductRegistered 이벤트 페이로드의 fasstoProductCode 로 채워지며 이후 불변(updatable=false).
-- 기존 row(운영 13건)는 임시 placeholder(LPAD id 22자) 로 백필 후 NOT NULL + UNIQUE 강제한다.
-- 이 placeholder 는 product 테이블의 백필 placeholder(V20260427)와 동일 정책이며,
-- 운영 13건의 실제 Fastto 운영 코드 매핑은 #5116 import API 발행 시 별도 처리한다.

-- 1. fassto_product_code 컬럼 추가 (nullable로 먼저 생성)
ALTER TABLE fassto_goods_registration
    ADD COLUMN fassto_product_code VARCHAR(25);

-- 2. 기존 데이터 백필: id 기반 22자 placeholder (Base62 인코딩 길이와 동일)
UPDATE fassto_goods_registration
SET fassto_product_code = LPAD(id::text, 22, '0')
WHERE fassto_product_code IS NULL;

-- 3. NOT NULL 제약 추가
ALTER TABLE fassto_goods_registration
    ALTER COLUMN fassto_product_code SET NOT NULL;

-- 4. UNIQUE 제약 추가
ALTER TABLE fassto_goods_registration
    ADD CONSTRAINT uk_fassto_goods_registration_fassto_product_code UNIQUE (fassto_product_code);
