-- Ids become BIGINT (Java Long) so the table can grow beyond 2^31 rows.
ALTER TABLE product ALTER COLUMN id TYPE BIGINT;
ALTER SEQUENCE product_id_seq AS BIGINT;

-- Ownership is a plain user id from auth-service: no foreign key across service databases.
-- Existing rows keep NULL owners (they were created before login existed).
ALTER TABLE product
    ADD COLUMN description VARCHAR(1000),
    ADD COLUMN owner_id    UUID,
    ADD COLUMN owner_name  VARCHAR(100),
    ADD COLUMN created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN version     BIGINT      NOT NULL DEFAULT 0;

CREATE INDEX idx_product_owner_id ON product (owner_id);
CREATE INDEX idx_product_name_lower ON product (lower(name));
