-- Runs once, when the Docker volume is first created.
-- product_db is created by POSTGRES_DB; each service owns its own database.
CREATE DATABASE auth_db;
