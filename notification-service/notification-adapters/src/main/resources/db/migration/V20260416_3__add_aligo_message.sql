CREATE TABLE aligo_message (
    id              BIGSERIAL       PRIMARY KEY,
    sender_key      VARCHAR(100)    NOT NULL,
    template_code   VARCHAR(255)    NOT NULL,
    sender          VARCHAR(20)     NOT NULL,
    message_type    VARCHAR(10)     NOT NULL DEFAULT 'AT',
    content         TEXT            NOT NULL,
    title           VARCHAR(255),
    buttons         TEXT,
    message_status  VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED',
    aligo_mid       VARCHAR(100)    UNIQUE,
    success_count   INT             NOT NULL DEFAULT 0,
    fail_count      INT             NOT NULL DEFAULT 0,
    requested_at    TIMESTAMP       NOT NULL DEFAULT NOW(),
    sent_at         TIMESTAMP,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    modified_at     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE aligo_message_recipient (
    id                  BIGSERIAL       PRIMARY KEY,
    aligo_message_id    BIGINT          NOT NULL REFERENCES aligo_message(id) ON DELETE CASCADE,
    receiver            VARCHAR(20)     NOT NULL,
    recv_name           VARCHAR(100),
    msg_id              VARCHAR(100),
    recipient_status    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    rslt                VARCHAR(20),
    rslt_message        VARCHAR(500),
    sent_at             TIMESTAMP,
    result_at           TIMESTAMP,
    report_at           TIMESTAMP
);

CREATE INDEX idx_aligo_message_status_requested ON aligo_message(message_status, requested_at);
CREATE INDEX idx_aligo_message_recipient_message_id ON aligo_message_recipient(aligo_message_id);
