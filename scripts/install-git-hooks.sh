#!/usr/bin/env bash
#
# Git hook 설치 스크립트
#
# `git config core.hooksPath scripts/git-hooks` 로 hook 디렉토리를 전환한다.
# Git 2.9+ 필요. 워크트리/메인 repo 모두에 동일하게 적용되어 절대 경로
# 심볼릭 링크 깨짐 문제를 회피한다.
#
# 사용:
#   bash scripts/install-git-hooks.sh
#
# 해제:
#   git config --unset core.hooksPath
#
# 주의:
#   - 본 명령 실행 후에는 .git/hooks/ 디렉토리의 기존 개인 hook이 무시된다.
#     (git이 core.hooksPath만 참조하기 때문)
#   - 개인 hook을 유지하려면 scripts/git-hooks/ 디렉토리에 추가하거나
#     본 가드 적용 대신 hooks 수동 심볼릭 링크 방식을 사용한다.
#

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOKS_DIR="$REPO_ROOT/scripts/git-hooks"

if [[ ! -d "$HOOKS_DIR" ]]; then
    echo "ERROR: $HOOKS_DIR not found." >&2
    exit 1
fi

for hook in "$HOOKS_DIR"/*; do
    [[ -f "$hook" ]] || continue
    chmod +x "$hook"
done

git -C "$REPO_ROOT" config core.hooksPath scripts/git-hooks

echo "[OK] core.hooksPath = scripts/git-hooks"
echo ""
echo "기존 .git/hooks/ 의 개인 hook은 더 이상 실행되지 않습니다."
echo "해제: git config --unset core.hooksPath"
