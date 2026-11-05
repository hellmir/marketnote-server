-- ============================================================
-- image_read_model_resized_files
-- ============================================================
CREATE TABLE image_read_model_resized_files (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    modified_at TIMESTAMP(6),
    image_read_model_id BIGINT NOT NULL,
    size VARCHAR(63) NOT NULL,
    storage_url VARCHAR(511) NOT NULL,
    sort_order INTEGER NOT NULL,
    CONSTRAINT fk_image_read_model_resized_file_image_read_model
        FOREIGN KEY (image_read_model_id) REFERENCES image_read_models (id) ON DELETE CASCADE,
    CONSTRAINT uk_image_read_model_resized_file_model_size UNIQUE (image_read_model_id, size)
);

CREATE INDEX idx_image_read_model_resized_file_model
    ON image_read_model_resized_files (image_read_model_id);
