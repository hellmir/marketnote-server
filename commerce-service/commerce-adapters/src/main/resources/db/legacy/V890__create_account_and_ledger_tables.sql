-- 계정과목 테이블
CREATE TABLE IF NOT EXISTS account
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    name
    VARCHAR
(
    31
) NOT NULL UNIQUE,
    account_type VARCHAR
(
    15
) NOT NULL,
    status VARCHAR
(
    15
) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP
    );

-- 장부 거래 테이블
CREATE TABLE IF NOT EXISTS ledger_transaction
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    transaction_type
    VARCHAR
(
    31
) NOT NULL,
    target_type VARCHAR
(
    15
) NOT NULL,
    target_id BIGINT,
    description VARCHAR
(
    255
),
    idempotency_key VARCHAR
(
    255
) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 장부 항목 테이블 (복식 부기 차변/대변)
CREATE TABLE IF NOT EXISTS ledger_entry
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    account_id
    BIGINT
    NOT
    NULL
    REFERENCES
    account
(
    id
),
    transaction_id BIGINT NOT NULL REFERENCES ledger_transaction
(
    id
),
    amount BIGINT NOT NULL CHECK
(
    amount >
    0
),
    transaction_type VARCHAR
(
    15
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_ledger_entry_account_id ON ledger_entry(account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_transaction_id ON ledger_entry(transaction_id);
CREATE INDEX IF NOT EXISTS idx_ledger_transaction_target ON ledger_transaction(target_type, target_id);

-- 시드 데이터: 초기 계정과목
INSERT INTO account (name, account_type, status)
VALUES ('매출채권_PG', 'ASSET', 'ACTIVE'),
       ('보통예금', 'ASSET', 'ACTIVE'),
       ('미지급금_판매자', 'LIABILITY', 'ACTIVE'),
       ('플랫폼수수료수익', 'REVENUE', 'ACTIVE'),
       ('PG수수료비용', 'EXPENSE', 'ACTIVE') ON CONFLICT (name) DO NOTHING;
