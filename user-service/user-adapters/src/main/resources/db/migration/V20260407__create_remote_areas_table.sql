CREATE TABLE IF NOT EXISTS remote_areas
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    zip_code
    VARCHAR
(
    5
) NOT NULL UNIQUE,
    remote_area_type VARCHAR
(
    30
) NOT NULL,
    region_name VARCHAR
(
    100
) NOT NULL,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX idx_remote_areas_zip_code_status ON remote_areas (zip_code, status);
