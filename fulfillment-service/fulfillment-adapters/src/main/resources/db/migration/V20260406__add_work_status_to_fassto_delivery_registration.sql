-- #1928: 풀필먼트 작업 상태 조회 API - work_status 컬럼 추가
ALTER TABLE fassto_delivery_registration
    ADD COLUMN work_status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED';
