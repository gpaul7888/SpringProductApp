CREATE TABLE app_user (
    id            UUID         PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

-- Emails are stored lower-cased by the application; the index enforces uniqueness.
CREATE UNIQUE INDEX uk_app_user_email ON app_user (email);
