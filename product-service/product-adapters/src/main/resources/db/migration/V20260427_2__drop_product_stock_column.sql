-- #5207: product 테이블 데드 stock 컬럼 제거
-- ProductJpaEntity.stock은 insertable=false로 선언만 되어 있고 코드에서 호출 0건.
-- DEFAULT 0으로 인서트만 되고 어디서도 갱신·조회되지 않는 데드 컬럼.
SET lock_timeout = '3s';
ALTER TABLE product DROP COLUMN IF EXISTS stock;
