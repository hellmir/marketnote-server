# Flyway 마이그레이션 롤백/복구 런북

## 핵심 원칙: forward-fix only

본 프로젝트는 Flyway Community Edition을 사용하며 undo 마이그레이션을 지원하지 않는다. 마이그레이션 실패 시 **롤백이 아닌 forward-fix(수정 마이그레이션 추가)** 로 해결한다.

이유:
- Flyway undo는 Teams/Pro 유료 기능
- 운영 데이터가 적용된 상태에서 임의 롤백은 데이터 손실 위험
- 마이그레이션 이력을 일관되게 유지 (schema_history 무결성)

## 시나리오별 대응 절차

### 1. 마이그레이션 SQL 자체 오류 (테이블명 오타, 컬럼 누락 등)

증상: `relation "X" does not exist`, `column "Y" does not exist` 등 (예: #4504 file-service V3)

대응:
1. 실패 마이그레이션의 schema_history 상태 확인
   ```sql
   SELECT * FROM flyway_schema_history WHERE success = false;
   ```
2. 실패 row 존재 시 두 가지 옵션:
   - **(권장) Flyway repair 실행**: `flyway repair` 명령 → (1) 실패 row 제거 + (2) 적용된 마이그레이션의 checksum을 현재 SQL 파일 기준으로 재계산. 시나리오 1에서는 (1) 효과만 의도되며 (2)는 부작용 없음 (이번 케이스는 SQL 미수정).
   - 수동 DELETE: `DELETE FROM flyway_schema_history WHERE version = 'X' AND success = false;`
3. SQL 파일 수정 후 재기동 → 마이그레이션 재실행

주의: V1처럼 이미 성공 적용된 파일은 절대 사후 수정 금지 (checksum 보호)

### 2. CREATE INDEX CONCURRENTLY 실패로 INVALID 인덱스 잔존

증상: ECS task killed → 재시작 → `IF NOT EXISTS`로 skip → 인덱스가 INVALID 상태로 영영 잔존 (예: #4501 product V10)

대응 옵션 A — 자동 (V14 패턴, 권장):
- 새 Vn 파일에 DO 블록 패턴 적용 (product-service V14 파일 참조 — `V14__recover_invalid_indexes_for_price_policy_and_product_category.sql`)
- 다음 배포 시 자동 복구

대응 옵션 B — 수동 DBA 개입 (긴급 시):
1. INVALID 인덱스 식별:
   ```sql
   SELECT n.nspname, c.relname FROM pg_class c
     JOIN pg_index i ON i.indexrelid = c.oid
     JOIN pg_namespace n ON n.oid = c.relnamespace
     WHERE NOT i.indisvalid
       AND n.nspname NOT IN ('pg_catalog','information_schema');
   ```
2. 수동 DROP (CONCURRENTLY 옵션 사용, **트랜잭션 외부에서 단독 실행 — psql autocommit 모드 또는 단일 statement 필요**):
   ```sql
   DROP INDEX CONCURRENTLY <schema>.<invalid_index_name>;
   ```
3. 다음 배포에서 마이그레이션 재실행으로 INDEX 정상 생성

### 3. mixed statement 파싱 에러 (transactional + non-transactional 공존)

증상: `Detected both transactional and non-transactional statements within the same migration` (예: #4507 community V7, #4514 product V12)

대응:
1. 해당 서비스 `application.yml`에 설정 추가:
   ```yaml
   spring:
     flyway:
       mixed: true
   ```
2. SQL 파일 자체는 수정하지 않음 (checksum 보호)
3. 재기동

### 4. checksum 불일치 (이미 적용된 SQL을 사후 수정한 경우)

증상: `Migration checksum mismatch` 또는 `Validate failed`

대응:
1. SQL 파일을 적용 시점의 원본으로 되돌림
2. 새로운 변경은 새 Vn 파일로 추가 — 프로젝트 선례에 따라 날짜 기반 (`V20260420__fix_xxx.sql`) 또는 다음 시퀀스 번호 (`V13__fix_xxx.sql`) 사용
3. 그래도 변경이 필요하면: `flyway repair`로 schema_history checksum 재계산 (운영에서는 신중하게)

## Flyway CLI 요약 (운영 직접 실행 시)

```bash
# schema_history 상태 확인
flyway info -url=jdbc:postgresql://<host>:5432/<db> -user=<u> -password=<p>

# 실패 row 정리 + checksum 재계산 (시나리오 1·4에 한정 사용 — 무분별 실행 금지)
flyway repair -url=... -user=... -password=...

# 마이그레이션 적용
flyway migrate -url=... -user=... -password=...

# 검증 (checksum + 순서 확인)
flyway validate -url=... -user=... -password=...
```

## Jenkins Pipeline 재실행 방법 (#4503 분리 후)

`Run Flyway Migration` stage 실패 시:
1. Jenkins UI → 빌드 실패 페이지 → "Replay" 또는 "Rebuild"
2. 사전에 SQL 파일 수정 + push 필요 (forward-fix)
3. stage 단독 재실행은 비권장 — 전체 파이프라인을 다시 돌려 일관성 확보

## 참고 문서

- cautions.md "Flyway 마이그레이션" 섹션 — 기본 작성 규칙
- product-service `V14__recover_invalid_indexes_for_price_policy_and_product_category.sql` — INVALID 인덱스 복구 V14 패턴 실제 적용 선례 (#4501)
