# Concurrent Indexes Runbook

## 배경

`CREATE INDEX CONCURRENTLY` 는 긴 실행 시간 동안 테이블의 DML 을 차단하지 않기 위해 PostgreSQL 이 제공하는 논-블로킹 인덱스 생성 기능이다. 다음 제약이 있다.

- 트랜잭션 내부에서 실행 불가 (`autocommit` 필요)
- 실행 중 동일 테이블에 다른 락 커넥션이 남아 있으면 자기 교착 (self-deadlock)

product-service V12 에서 Flyway `executeInTransaction=false` 로 실행해도 Flyway 가 보유한 **schema history 락 커넥션**이 남아 있어 CONCURRENTLY 와 교착, 17분+ hang 후 fail 하는 이슈(#4551)가 발생했다.

해결책: CONCURRENTLY 인덱스 생성을 Flyway 에서 분리하여 Jenkins 별도 스테이지에서 psql 로 직접 실행한다.

## 파이프라인 구조

```
Run Flyway Migration
    → Run Concurrent Indexes   (concurrent-indexes 디렉터리가 존재하는 서비스만, psql 직접 실행)
    → Override Spring Flyway Enabled=false
    → Register Task Definition
```

- 활성화 조건: `Jenkinsfile` 의 `Run Concurrent Indexes` 스테이지는 `fileExists(${env.SERVICE_DIRECTORY}/src/main/resources/db/concurrent-indexes)` 인 서비스에 대해서만 실행
- 스크립트 디렉터리: `{service}/{service-adapters}/src/main/resources/db/concurrent-indexes/`
  - 예: `product-service/product-adapters/src/main/resources/db/concurrent-indexes/`
- 실행 래퍼: `jenkins/run-concurrent-indexes.sh`
- 파일 네이밍: `NNN__{설명}.sql` (세 자리 숫자 정렬 키 + double underscore)
- 실행 순서: 파일명 사전순

### 서비스명 → 어댑터 모듈 매핑

| SERVICE_NAME            | 어댑터 모듈 디렉터리         |
| ----------------------- | -------------------------- |
| `user-service`          | `user-adapters`            |
| `product-service`       | `product-adapters`         |
| `commerce-service`      | `commerce-adapters`        |
| `community-service`     | `community-adapters`       |
| `reward-service`        | `reward-adapters`          |
| `fulfillment-service`   | `fulfillment-adapters`     |
| `file-service`          | `file-adapters`            |
| `notification-service`  | `notification-adapters`    |

## 스크립트 작성 규칙

- 모든 문장은 **idempotent** 해야 한다. 반복 실행되어도 결과가 같아야 한다.
  - `CREATE INDEX CONCURRENTLY IF NOT EXISTS ...`
  - `CREATE EXTENSION IF NOT EXISTS ...`
  - 마이그레이션 방식의 `ALTER` 등 비멱등 DDL 금지 (그런 DDL 은 Flyway 로 유지한다)
- `BEGIN; ... COMMIT;` 블록 금지. psql `-f` 기본 동작(auto-commit)에 의존.
- 파일 헤더에 스크립트 목적과 재시도 안전성 보장 근거를 주석으로 남긴다.

## 신규 CONCURRENTLY 마이그레이션 추가 가이드

1. `{service}/{service-adapters}/src/main/resources/db/concurrent-indexes/` 아래에 SQL 파일을 추가
   - 예: `product-service/product-adapters/src/main/resources/db/concurrent-indexes/002__order_covering_indexes.sql`
2. 모든 문장을 idempotent 형태로 작성 (`IF NOT EXISTS`)
3. Flyway 마이그레이션(`V{n}__...sql`)에 CONCURRENTLY DDL 을 추가하지 않는다
4. 개발자는 로컬에서 스크립트 직접 실행 가능 (환경변수 전달 방식):
   ```bash
   DB_URL="postgresql://localhost:5432/marketnote_product" \
   DB_USER="$DB_USER" \
   DB_PASSWORD="$DB_PASSWORD" \
   bash jenkins/run-concurrent-indexes.sh \
     ./product-service/product-adapters/src/main/resources/db/concurrent-indexes
   ```

## 대상 서비스 확장 방법

현재 product-service 에만 `concurrent-indexes/` 디렉터리가 존재한다. 다른 서비스로 확장할 때:

1. 해당 서비스의 `{service}/{service-adapters}/src/main/resources/db/concurrent-indexes/` 디렉터리를 생성하고 SQL 파일을 추가
2. **Jenkinsfile 수정 불필요.** `Run Concurrent Indexes` 스테이지는 `fileExists(...)` 로 디렉터리 존재 여부를 감지해 자동으로 활성화된다
3. 디렉터리가 없는 서비스는 스테이지 자체가 skip 되므로 no-op (파이프라인 시간 영향 없음)

## 장애 복구

### INVALID 인덱스 탐지

CONCURRENTLY 생성이 실패하면 PostgreSQL 은 빈 껍데기 인덱스를 `indisvalid = false` 상태로 남긴다.
이 상태에서 `CREATE INDEX CONCURRENTLY IF NOT EXISTS` 는 "이미 존재"로 판단하여 재생성을 skip 한다.

탐지 쿼리 (bash 래퍼와 동일하게 `public` 스키마 한정):

```sql
SELECT c.relname AS invalid_index_name,
       n.nspname AS schema,
       t.relname AS table_name
  FROM pg_class c
  JOIN pg_index i ON i.indexrelid = c.oid
  JOIN pg_class t ON t.oid = i.indrelid
  JOIN pg_namespace n ON n.oid = c.relnamespace
 WHERE NOT i.indisvalid
   AND n.nspname = 'public'
   AND c.relname LIKE 'idx_%'
 ORDER BY t.relname, c.relname;
```

### 자동 복구

`jenkins/run-concurrent-indexes.sh` 가 실행 시작 시 위 탐지 쿼리를 수행하고, 찾은 INVALID 인덱스를 모두 `DROP INDEX CONCURRENTLY IF EXISTS` 로 드롭한 뒤 본 스크립트를 실행한다. 따라서 일시적인 fail 은 다음 Jenkins 빌드에서 자동 복구된다.

### 수동 복구

자동 복구가 동작하지 않거나 DB 에 직접 접근해야 할 때:

```bash
psql "$PRODUCT_SERVICE_DB_URL" -U "$DB_USERNAME" \
  -c 'DROP INDEX CONCURRENTLY IF EXISTS "idx_product_name_trgm";'
psql "$PRODUCT_SERVICE_DB_URL" -U "$DB_USERNAME" \
  -c 'DROP INDEX CONCURRENTLY IF EXISTS "idx_product_brand_name_trgm";'
```

드롭 후 Jenkins 파이프라인을 재실행하면 스크립트가 다시 인덱스를 생성한다.

## 기존 V12 마이그레이션 취급 (CRITICAL)

`V12__add_pg_trgm_search_indexes.sql` 은 **이미 모든 환경(local/QA/prod)에서 실행 완료 상태**이고 Flyway schema history 에 체크섬이 기록되어 있다. **원본을 유지**해야 하며 어떤 편집(주석 추가 포함)도 금지한다. 편집 시 체크섬이 달라져 다음 Flyway 실행이 `FlywayValidateException` 으로 실패한다.

- **V12 를 수정하지 않는 한 `flyway repair` 필요 없음.**
- 향후 pg_trgm 같은 신규 CONCURRENTLY 인덱스는 `db/migration/` 이 아니라 `db/concurrent-indexes/` 에 추가한다.
- 예외적으로 V12 편집이 불가피한 경우에만 대상 DB 에서 `flyway repair` 를 수행해 체크섬을 재동기화한 뒤 배포한다.
