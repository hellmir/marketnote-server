#!/usr/bin/env bash
#
# Flyway 적용 완료 마이그레이션 파일 변경 차단 가드
#
# 적용 완료된 V*.sql 파일은 byte 단위로 immutable이어야 한다.
# Flyway는 매 부팅 시 schema_history.checksum과 로컬 파일 byte의 재계산
# checksum을 비교하므로, byte 변경(공백/줄바꿈/IDE auto-format 포함)만으로도
# validate가 실패하여 배포가 차단된다.
#
# 동작:
#   1) origin/dev 또는 staged 영역과 비교하여 V*.sql 파일의 변경 종류를 확인
#   2) M(modified) / D(deleted) / R(renamed) 인 파일이 있으면 exit 1
#   3) A(added) 만 있으면 통과 (신규 마이그레이션 파일 추가는 허용)
#
# 모드:
#   기본 (--ci):  origin/dev...HEAD 비교 (CI/PR 빌드용)
#   --staged:     staged 변경분 비교 (pre-commit hook용)
#
# 종료 코드:
#   0  통과 (또는 변경 없음)
#   1  immutability 위반 발견
#   2  잘못된 사용 (인자 오류 등)
#
# 사용:
#   bash scripts/check-flyway-immutability.sh
#   bash scripts/check-flyway-immutability.sh --ci
#   bash scripts/check-flyway-immutability.sh --staged
#

set -euo pipefail

MODE="${1:---ci}"

case "$MODE" in
    --ci)
        # CI 모드: PR 변경분(origin/dev...HEAD) 검사
        # origin/dev fetch 보장 (실패 시 다음 ref 검증에서 명확한 에러 출력)
        git fetch --no-tags --quiet origin dev || true
        if ! git rev-parse --verify --quiet origin/dev >/dev/null; then
            echo "[FAIL] origin/dev ref가 없습니다. 'git fetch origin dev' 후 재실행하세요." >&2
            exit 1
        fi
        DIFF_OUTPUT=$(git diff --name-status origin/dev...HEAD -- ':(glob)**/db/migration/V*.sql')
        ;;
    --staged)
        # 로컬 모드: 현재 staged된 변경분 검사
        DIFF_OUTPUT=$(git diff --cached --name-status -- ':(glob)**/db/migration/V*.sql')
        ;;
    *)
        echo "Usage: $0 [--ci|--staged]" >&2
        exit 2
        ;;
esac

# M(modified) / D(deleted) / R(renamed) 모두 위반으로 분류
VIOLATIONS=$(echo "$DIFF_OUTPUT" | awk '$1 == "M" || $1 == "D" || $1 ~ /^R/ { print }')

if [[ -z "$VIOLATIONS" ]]; then
    echo "[OK] Flyway immutability check passed: no modifications to applied migration files"
    exit 0
fi

cat >&2 <<EOF
[FAIL] Flyway immutability violation detected

이미 적용된 Flyway 마이그레이션 파일이 변경(M)/삭제(D)/이름변경(R) 되었습니다.
적용 완료 V*.sql 파일은 byte 단위로 immutable입니다.

원인: byte 변경만으로 Flyway schema_history.checksum과 mismatch가 발생하여
      QA/운영 배포 시 validate 실패로 배포가 차단됩니다.

위반 파일:
$VIOLATIONS

해결 방법:
  - SQL semantic이 동일하다면 (IDE auto-format 등): 원본 byte로 복원
      git checkout origin/dev -- <파일경로>
      ※ 주의: 로컬 변경을 즉시 덮어씁니다. semantic 변경분이 있다면 먼저 신규 V 파일로 분리.
  - SQL semantic을 변경해야 한다면: 신규 V<날짜>__*.sql 파일 추가
      예) V$(date +%Y%m%d)__alter_xxx.sql

자세한 가이드: docs/infra/flyway-immutability-guide.md
EOF

exit 1
