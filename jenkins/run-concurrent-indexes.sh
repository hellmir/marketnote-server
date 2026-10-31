#!/usr/bin/env bash
#
# CREATE INDEX CONCURRENTLY 전용 실행 스크립트
#
# Flyway schema history 락 커넥션과 CONCURRENTLY 가 자기 교착하는 이슈를 회피하기 위해
# Flyway 마이그레이션과 분리된 Jenkins 스테이지에서 psql 로 직접 실행한다.
#
# 동작:
#   1) INVALID 상태의 public.idx_* 인덱스를 탐지 → DROP INDEX CONCURRENTLY IF EXISTS
#      (CREATE INDEX CONCURRENTLY IF NOT EXISTS 는 INVALID 인덱스도 존재로 간주하여 skip 하므로
#       선행 DROP 없이는 재시도가 불가능하다.)
#   2) 전달된 디렉터리 내 *.sql 파일을 파일명 사전순으로 psql -f 실행
#   3) 한 파일이라도 실패하면 즉시 중단 (set -euo pipefail + ON_ERROR_STOP=1)
#
# 인자:
#   $1 SCRIPT_DIR   — .sql 파일들이 들어 있는 디렉터리 (기본: /concurrent-indexes)
#
# 환경변수:
#   DB_URL       — jdbc: 또는 postgresql:// 형식 모두 허용, jdbc: 접두사는 제거됨
#   DB_USER      — DB 사용자명
#   DB_PASSWORD  — DB 비밀번호 (PGPASSWORD 로 psql 에 전달)
#
# psql 공통 플래그:
#   -w : 비대화형 (패스워드 프롬프트 차단, hang 방지)
#   -X : ~/.psqlrc 무시 (환경 오염 방지)
#   -v ON_ERROR_STOP=1 : SQL 오류 즉시 중단

set -euo pipefail

SCRIPT_DIR="${1:-/concurrent-indexes}"
DB_URL="${DB_URL:?DB_URL env required}"
DB_USER="${DB_USER:?DB_USER env required}"
DB_PASSWORD="${DB_PASSWORD:?DB_PASSWORD env required}"

# psql 은 libpq URL (postgresql://...) 만 인식한다. jdbc: 접두사 제거.
PSQL_URL="${DB_URL#jdbc:}"

export PGPASSWORD="$DB_PASSWORD"

echo "==== Concurrent indexes runner ===="
echo "SCRIPT_DIR = $SCRIPT_DIR"
echo "DB_USER    = $DB_USER"
echo "PSQL_URL   = ${PSQL_URL%%\?*}"   # 쿼리스트링 제거 후 출력

# 1) INVALID 인덱스 탐지 → DROP (public 스키마의 idx_* 한정)
echo "---- Detecting INVALID indexes ----"
INVALID_IDX=$(psql "$PSQL_URL" -U "$DB_USER" -w -X -At -v ON_ERROR_STOP=1 -c \
  "SELECT c.relname
     FROM pg_class c
     JOIN pg_index i ON i.indexrelid = c.oid
     JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE NOT i.indisvalid
      AND n.nspname = 'public'
      AND c.relname LIKE 'idx_%'")

if [ -z "$INVALID_IDX" ]; then
  echo "No INVALID indexes found."
else
  echo "Found INVALID indexes:"
  echo "$INVALID_IDX"
  while IFS= read -r idx; do
    [ -z "$idx" ] && continue
    echo "Dropping INVALID index: $idx"
    psql "$PSQL_URL" -U "$DB_USER" -w -X -v ON_ERROR_STOP=1 \
      -c "DROP INDEX CONCURRENTLY IF EXISTS \"$idx\""
  done <<< "$INVALID_IDX"
fi

# 2) 디렉터리 내 *.sql 파일 사전순 실행
echo "---- Applying concurrent index scripts ----"
shopt -s nullglob
FILES=( "$SCRIPT_DIR"/*.sql )
shopt -u nullglob

if [ ${#FILES[@]} -eq 0 ]; then
  echo "No *.sql files found in $SCRIPT_DIR. Nothing to do."
  exit 0
fi

# 파일명 사전순 정렬
mapfile -t SORTED < <(printf '%s\n' "${FILES[@]}" | sort)

for sql in "${SORTED[@]}"; do
  echo "Running: $sql"
  psql "$PSQL_URL" -U "$DB_USER" -w -X -v ON_ERROR_STOP=1 -f "$sql"
done

echo "==== Concurrent indexes applied successfully ===="
