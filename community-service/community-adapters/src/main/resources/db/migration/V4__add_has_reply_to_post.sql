-- ============================================================
-- post 테이블에 has_reply 비정규화 컬럼 추가 (답변 존재 여부)
-- 기존 findByBoardAndFiltersOrderByAnswered의 EXISTS 서브쿼리 비용 제거용
-- 답변(Reply) 등록/삭제 시점에 서비스 레이어가 동기 갱신
-- ============================================================
ALTER TABLE post
    ADD COLUMN has_reply BOOLEAN NOT NULL DEFAULT false;

-- 기존 데이터 backfill: ACTIVE 상태 답변이 존재하는 부모 게시글만 true로 설정
UPDATE post p
SET has_reply = true
WHERE EXISTS (
    SELECT 1
    FROM post c
    WHERE c.parent_id = p.id
      AND c.status = 'ACTIVE'
);

-- 답변 여부 정렬 최적화용 복합 인덱스 (board + has_reply + id DESC 조회 패턴)
CREATE INDEX IF NOT EXISTS idx_post_board_has_reply_id
    ON post (board, has_reply, id);

-- 부모 게시글 hasReply 재계산 시 자식 활성 존재 여부 조회(existsByParentIdAndStatus) 최적화
CREATE INDEX IF NOT EXISTS idx_post_parent_status
    ON post (parent_id, status);
