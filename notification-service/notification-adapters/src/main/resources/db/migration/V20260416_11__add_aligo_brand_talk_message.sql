CREATE TABLE aligo_brand_talk_message (
    id               BIGSERIAL       PRIMARY KEY,
    sender_key       VARCHAR(100)    NOT NULL,
    sender           VARCHAR(20)     NOT NULL,
    kakao_target     VARCHAR(10)     NOT NULL,
    advert_yn        BOOLEAN         NOT NULL DEFAULT FALSE,
    template_code    VARCHAR(255)    NOT NULL,
    message_status   VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED',
    cost_type        VARCHAR(10),
    unit_cost        INT,
    total_cost       INT,
    aligo_mid        VARCHAR(100)    UNIQUE,
    success_count    INT             NOT NULL DEFAULT 0,
    fail_count       INT             NOT NULL DEFAULT 0,
    failover_enabled BOOLEAN         NOT NULL DEFAULT FALSE,
    failover_subject VARCHAR(100),
    failover_type    VARCHAR(10),
    requested_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    sent_at          TIMESTAMP,
    status           VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    modified_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE aligo_brand_talk_message_recipient (
    id                          BIGSERIAL       PRIMARY KEY,
    brand_talk_message_id       BIGINT          NOT NULL REFERENCES aligo_brand_talk_message(id) ON DELETE CASCADE,
    receiver                    VARCHAR(20)     NOT NULL,
    receiver_message            TEXT            NOT NULL,
    msg_id                      VARCHAR(100),
    recipient_status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    rslt                        VARCHAR(20),
    rslt_message                VARCHAR(500)
);

CREATE INDEX idx_brand_talk_message_status ON aligo_brand_talk_message(message_status, requested_at);
CREATE INDEX idx_brand_talk_message_recipient_message_id ON aligo_brand_talk_message_recipient(brand_talk_message_id);
