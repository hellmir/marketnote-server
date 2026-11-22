-- Fastto 운영 시스템에서 사용할 product 운영 코드 컬럼 추가.
-- 신규 상품은 product_key(UUID v7) 기반 Base62 22자 인코딩으로 발급되며 이후 불변(updatable=false).
-- 기존 행(13건 운영 + 테스트 데이터)은 임시 placeholder(LPAD id 22자)로 백필 후 NOT NULL + UNIQUE 강제.
-- 운영 13건의 실제 Fastto 운영 코드 매핑은 Phase B(#5116) import API 발행 시 별도 처리한다.

-- 1. fassto_product_code 컬럼 추가 (nullable로 먼저 생성)
ALTER TABLE product
    ADD COLUMN fassto_product_code VARCHAR(25);

-- 2. 기존 데이터 백필: id 기반 22자 placeholder (Base62 인코딩 길이와 동일)
UPDATE product
SET fassto_product_code = LPAD(id::text, 22, '0')
WHERE fassto_product_code IS NULL;

-- 3. NOT NULL 제약 추가
ALTER TABLE product
    ALTER COLUMN fassto_product_code SET NOT NULL;

-- 4. UNIQUE 제약 추가
ALTER TABLE product
    ADD CONSTRAINT uk_product_fassto_product_code UNIQUE (fassto_product_code);
