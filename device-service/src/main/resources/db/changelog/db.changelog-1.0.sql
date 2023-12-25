--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS devices
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tuya_device_id      VARCHAR     UNIQUE NOT NULL,
    owner_id            BIGINT             NOT NULL,
    home_id             BIGINT             NOT NULL,
    room_id             BIGINT,
    name                VARCHAR(64)        NOT NULL,
    category            VARCHAR(64)        NOT NULL
);
--rollback DROP TABLE devices;
