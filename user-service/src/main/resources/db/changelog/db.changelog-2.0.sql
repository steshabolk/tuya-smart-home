--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)     PRIMARY KEY,
    lock_until TIMESTAMP        NOT NULL,
    locked_at  TIMESTAMP        NOT NULL,
    locked_by  VARCHAR(255)     NOT NULL
);
--rollback DROP TABLE shedlock;
