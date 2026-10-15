-- ============================================================
-- user_fingerprint (디바이스 핑거프린트 수집)
-- ============================================================
CREATE TABLE user_fingerprint (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fingerprint VARCHAR(255) NOT NULL,
    source VARCHAR(20) NOT NULL,
    first_seen_at TIMESTAMP(6) NOT NULL,
    last_seen_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_user_fingerprint_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ux_user_fingerprint_user_id_fingerprint UNIQUE (user_id, fingerprint)
);

CREATE INDEX ix_user_fingerprint_fingerprint ON user_fingerprint(fingerprint);
