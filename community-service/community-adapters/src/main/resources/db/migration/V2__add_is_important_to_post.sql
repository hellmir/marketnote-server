-- ============================================================
-- post 테이블에 is_important 컬럼 추가 (중요 공지 여부)
-- NOTICE + ANNOUNCEMENT 게시글에만 의미가 있으며, 그 외는 false로 저장
-- ============================================================
ALTER TABLE post
    ADD COLUMN is_important BOOLEAN NOT NULL DEFAULT false;
