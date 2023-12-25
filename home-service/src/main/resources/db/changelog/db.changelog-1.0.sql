--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS homes
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(64)        NOT NULL,
    address     VARCHAR(128)
);
--rollback DROP TABLE homes;

--changeset steshabolk:2
CREATE TABLE IF NOT EXISTS rooms
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    home_id     BIGINT REFERENCES homes (id) ON DELETE CASCADE,
    name        VARCHAR(64)        NOT NULL
);
--rollback DROP TABLE rooms;

--changeset steshabolk:3
ALTER TABLE IF EXISTS homes
ADD COLUMN IF NOT EXISTS owner_id BIGINT NOT NULL DEFAULT -1;

--changeset steshabolk:4
ALTER TABLE IF EXISTS homes
ALTER COLUMN owner_id DROP DEFAULT;
