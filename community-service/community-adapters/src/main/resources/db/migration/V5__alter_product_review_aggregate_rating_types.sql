ALTER TABLE product_review_aggregate
    ALTER COLUMN total_rating TYPE BIGINT USING ROUND(total_rating)::BIGINT;

ALTER TABLE product_review_aggregate
    ALTER COLUMN average_rating TYPE NUMERIC(3,2) USING ROUND(average_rating::numeric, 2);
