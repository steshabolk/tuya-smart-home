--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS users
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(30)        NOT NULL,
    username    VARCHAR(30) UNIQUE NOT NULL,
    password    VARCHAR            NOT NULL
);
--rollback DROP TABLE users;

--changeset steshabolk:2
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jwt_id          VARCHAR(36) UNIQUE NOT NULL,
    expires_at      TIMESTAMP          NOT NULL
);
--rollback DROP TABLE refresh_tokens;
