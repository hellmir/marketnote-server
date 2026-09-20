ALTER TABLE users
    ADD COLUMN penalty_count INT NOT NULL DEFAULT 0
        CONSTRAINT chk_users_penalty_count_non_negative CHECK (penalty_count >= 0);

CREATE TABLE user_penalty_histories (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    previous_count INT          NOT NULL CHECK (previous_count >= 0),
    current_count  INT          NOT NULL CHECK (current_count >= 0),
    reason         VARCHAR(500) NOT NULL,
    created_by     BIGINT       NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_penalty_histories_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_penalty_histories_user_id_created_at
    ON user_penalty_histories (user_id, created_at DESC);
