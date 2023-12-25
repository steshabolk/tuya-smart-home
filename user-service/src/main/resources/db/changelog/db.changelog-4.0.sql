--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS telegram_tokens
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    token       VARCHAR(36)     UNIQUE NOT NULL
);
--rollback DROP TABLE telegram_token;
