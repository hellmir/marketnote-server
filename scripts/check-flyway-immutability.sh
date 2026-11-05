#!/usr/bin/env bash
#
# Flyway 적용 완료 마이그레이션 파일 변경 차단 가드 (fail-closed)
#
# 적용 완료된 V*.sql 파일은 byte 단위로 immutable이어야 한다.
# Flyway는 매 부팅 시 schema_history.checksum과 로컬 파일 byte의 재계산
# checksum을 비교하므로, byte 변경(공백/줄바꿈/IDE auto-format 포함)만으로도
# validate가 실패하여 배포가 차단된다.
#
# 동작:
#   1) origin/dev 와 HEAD 를 비교하여 V*.sql 파일의 변경 종류를 확인
#   2) M(modified) / D(deleted) / R(renamed) 인 파일이 있으면 exit 1
#   3) A(added) 만 있으면 통과 (신규 마이그레이션 파일 추가는 허용)
#
# fail-closed 원칙:
#   - origin/dev fetch 실패 → exit 1
#   - origin/dev ref 부재 → exit 1
#   - V*.sql M/D/R 위반 발견 → exit 1
#   - 위반 없음만 exit 0
#
# NUL separator (-z) 적용:
#   파일명에 공백/탭/개행이 포함되어도 정확히 파싱된다.
#
# 종료 코드:
#   0  통과 (변경 없음 또는 A만 존재)
#   1  immutability 위반 발견 또는 origin/dev 획득 실패
#

set -euo pipefail

# origin/dev fetch (자격증명이 주입된 Jenkins 환경에서 호출 전제)
if ! git fetch --no-tags --quiet origin dev; then
    echo "[FAIL] origin/dev fetch 실패. Jenkins 자격증명 주입 상태를 확인하세요." >&2
    exit 1
fi

if ! git rev-parse --verify --quiet origin/dev >/dev/null; then
    echo "[FAIL] origin/dev ref가 없습니다. 'git fetch origin dev' 후 재실행하세요." >&2
    exit 1
fi

# NUL 구분자 출력을 그대로 받는다. $(...)는 후행 NUL을 자르지 않으므로 변수에 담는 대신 stream 파싱.
# -z 출력은 각 레코드(상태 + 경로 또는 상태 + 원경로 + 새경로)가 NUL 종료.
VIOLATIONS=""
VIOLATION_COUNT=0

while IFS= read -r -d '' status; do
    IFS= read -r -d '' path
    new_path=""

    case "$status" in
        R*|C*)
            # Rename(R) 은 `git diff` 기본 설정에서 감지된다. Copy(C) 는 --find-copies 미사용 시
            # 발생하지 않지만 방어적으로 처리. 둘 다 oldpath 다음 newpath 2-record 구조.
            if ! IFS= read -r -d '' new_path; then
                echo "[FAIL] git diff -z 출력 파싱 실패: rename/copy new_path 누락 (status=${status}, old=${path})" >&2
                exit 1
            fi
            ;;
    esac

    # A(added) 는 통과, 그 외 M/D/R/C 는 위반
    case "$status" in
        A)
            continue
            ;;
        M|D)
            VIOLATIONS+="${status}	${path}"$'\n'
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            ;;
        R*|C*)
            VIOLATIONS+="${status}	${path} -> ${new_path}"$'\n'
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            ;;
        T)
            # Type change (symlink 등) — 사용 사례 없지만 안전하게 위반 처리
            VIOLATIONS+="${status}	${path}"$'\n'
            VIOLATION_COUNT=$((VIOLATION_COUNT + 1))
            ;;
    esac
done < <(git diff -z --name-status origin/dev...HEAD -- ':(glob)**/db/migration/V*.sql')

if [[ "$VIOLATION_COUNT" -eq 0 ]]; then
    echo "[OK] Flyway immutability check passed: no modifications to applied migration files"
    exit 0
fi

cat >&2 <<EOF
[FAIL] Flyway immutability violation detected (${VIOLATION_COUNT}건)

이미 적용된 Flyway 마이그레이션 파일이 변경(M)/삭제(D)/이름변경(R) 되었습니다.
적용 완료 V*.sql 파일은 byte 단위로 immutable입니다.

원인: byte 변경만으로 Flyway schema_history.checksum과 mismatch가 발생하여
      QA/운영 배포 시 validate 실패로 배포가 차단됩니다.

위반 파일:
${VIOLATIONS}
해결 방법:
  - SQL semantic이 동일하다면 (IDE auto-format 등): 원본 byte로 복원
      git checkout origin/dev -- <파일경로>
      ※ 주의: 로컬 변경을 즉시 덮어씁니다. semantic 변경분이 있다면 먼저 신규 V 파일로 분리.
  - SQL semantic을 변경해야 한다면: 신규 V<날짜>__*.sql 파일 추가
EOF

exit 1
