-- [#4171] Review averageRating Float → BigDecimal 전환
-- review_aggregate_read_models.average_rating: REAL → NUMERIC(3,2)
-- product_catalog_read_models.review_average_rating: REAL → NUMERIC(3,2)

ALTER TABLE review_aggregate_read_models
    ALTER COLUMN average_rating TYPE NUMERIC(3,2) USING average_rating::numeric(3,2);

ALTER TABLE product_catalog_read_models
    ALTER COLUMN review_average_rating TYPE NUMERIC(3,2) USING review_average_rating::numeric(3,2);
