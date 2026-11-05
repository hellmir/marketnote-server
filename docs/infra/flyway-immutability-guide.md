# Flyway 적용 완료 마이그레이션 파일 immutability 가이드

## 배경

Flyway는 매 부팅 시 `flyway_schema_history.checksum` 컬럼과 로컬 V*.sql 파일의 byte 기준 재계산 checksum을 비교한다 (validate 단계). 두 값이 다르면 **byte 변경 1바이트만으로도 부팅이 차단**된다.

따라서 **이미 적용된 V*.sql 파일은 byte 단위로 immutable**이다. SQL semantic이 동일하더라도 (공백/줄바꿈/IDE auto-format) 파일 byte가 바뀌면 사고가 발생한다.

### 과거 사고 사례

| 시점 | 환경 | 원인 | 대응 |
|------|------|------|------|
| 운영 1회 (과거) | prod | V1/V2 byte 변경 후 배포 | schema_history checksum 직접 수정으로 임시 대응 |
| QA 1회 (2026-11-03) | QA | PR #4586에서 IntelliJ auto-format이 8개 서비스 42개 V 파일 byte 변경 | PR #4618 (notification 14개 revert) + 후속 7개 서비스 28개 revert |

## 4중 방어 가드

| 계층 | 강제력 | 역할 |
|------|--------|------|
| 1. CI (Jenkinsfile) | 강제 | PR 빌드에서 변경된 V 파일이 origin/dev 대비 modified/deleted/renamed면 빌드 실패 |
| 2. pre-commit hook | 약함 (설치 필요) | 로컬 commit 단계에서 차단, 빠른 피드백 |
| 3. .editorconfig | 보조 | `[**/db/migration/V*.sql]` 섹션으로 자동 포맷 영향 최소화 |
| 4. IntelliJ 설정 | 보조 (개인) | Save Actions의 Reformat code 비활성화 |

가드 1(CI)이 최종 방어선이다. 2~4는 빠른 피드백과 사고 사전 차단을 위한 보조 계층.

## pre-commit hook 설치

```bash
bash scripts/install-git-hooks.sh
```

`git config core.hooksPath scripts/git-hooks` 방식으로 설치된다 (Git 2.9+ 필요). 워크트리에서도 동일하게 동작한다.

설치 후 commit 시도 시 적용 완료 V 파일이 staged에 포함되어 있으면 차단된다.

긴급 우회 (디버깅 목적에만, CI에서 다시 차단됨):
```bash
git commit --no-verify
```

해제:
```bash
git config --unset core.hooksPath
```

> 주의: `core.hooksPath` 설정 후에는 `.git/hooks/` 디렉토리의 기존 개인 hook이 무시된다. 개인 hook을 유지하려면 `scripts/git-hooks/`에 추가하거나 본 가드 사용을 보류한다.

## IntelliJ 설정 (개인)

### 권장: Save Actions의 Reformat code 비활성화

가장 확실한 차단 방법. 자동 저장 시 SQL 파일이 재포맷되지 않도록 한다.

1. `Settings → Tools → Actions on Save`
2. `Reformat code` 체크 해제 (또는 SQL 파일 타입만 제외)

### 보완: 마이그레이션 디렉토리에 Code Style scope 적용

1. `Settings → Editor → Code Style → Scheme` 우측 톱니바퀴 → `Copy to Project`
2. 좌측 트리에서 `Scopes` 추가 → 패턴 `file:**/db/migration/V*.sql` 등록
3. SQL formatter가 해당 scope에 적용되지 않도록 별도 스킴 지정

### 비권장: `-- @formatter:off` 마커

```sql
-- @formatter:off
CREATE TABLE ...
-- @formatter:on
```

→ 이미 적용된 파일에 추가하면 다시 byte 변경이 되므로 신규 파일에만 적용. 가급적 사용하지 않는다.

## 위반 시 복구

CI 빌드가 immutability 검사로 실패한 경우:

### Case 1: byte만 변경되었고 SQL semantic은 동일 (auto-format 등)

```bash
# 위반된 V 파일을 origin/dev 시점 byte로 복원
git checkout origin/dev -- path/to/V20260422__xxx.sql
git add path/to/V20260422__xxx.sql
git commit --amend --no-edit  # 또는 새 커밋
```

> **주의**: `git checkout origin/dev -- <파일>`은 로컬 변경을 즉시 덮어쓴다. 만약 SQL semantic 변경분이 있었다면, 먼저 신규 V 파일로 분리한 뒤에 실행한다.

### Case 2: SQL semantic을 정말 변경해야 함

기존 V 파일 수정 금지. **신규 V 파일을 추가**하여 ALTER 형태로 표현:

```sql
-- 기존 V20260415__add_user_table.sql 은 그대로 두고
-- V20260423__alter_user_table_add_email.sql 신규 추가
ALTER TABLE users ADD COLUMN email VARCHAR(255);
```

### Case 3: 운영 schema_history checksum이 이미 dev와 다른 상태

운영 DB에서 직접 schema_history를 수정한 이력이 있으면 다음 배포 시 또 mismatch 발생 가능. 이 경우:

1. 운영 DB에서 `SELECT version, checksum FROM flyway_schema_history` 조회
2. 로컬 파일 byte의 재계산 checksum과 비교 (Flyway info 명령)
3. 불일치 시 별도 이슈 발행 후 운영 schema_history 정합화 (DBA 확인 필수)

## CI 가드 동작 원리

`scripts/check-flyway-immutability.sh`가 `git diff --name-status origin/dev...HEAD -- ':(glob)**/db/migration/V*.sql'` 결과를 파싱한다.

| diff 종류 | 의미 | 처리 |
|-----------|------|------|
| A | 신규 V 파일 추가 | 통과 |
| M | 기존 V 파일 byte 변경 | 빌드 실패 |
| D | 기존 V 파일 삭제 | 빌드 실패 |
| R | 기존 V 파일 이름 변경 | 빌드 실패 |

종료 코드:
- 0: 통과
- 1: immutability 위반
- 2: 스크립트 사용 오류 (인자 오류 등)

## FAQ

### Q. 단순 typo 수정도 허용되지 않나?

A. 허용되지 않는다. typo 수정도 byte 변경 → checksum mismatch. 신규 V 파일로 정정 마이그레이션을 추가해야 한다.

### Q. 마이그레이션 파일에 주석만 추가하고 싶다면?

A. 주석 추가도 byte 변경이므로 불가. 주석은 신규 V 파일에 작성하거나, 별도 문서 (예: `docs/db/migration-notes.md`)에 정리한다.

### Q. 로컬에서 아직 PR을 안 만들었는데 V 파일을 수정해도 되나?

A. **dev에 머지된 V 파일이면 안 된다**. 가드 기준은 origin/dev. 로컬 작업 중이라도 dev에 이미 있는 파일이면 차단된다. 새 마이그레이션을 만들고 거기에 추가/수정한다.
