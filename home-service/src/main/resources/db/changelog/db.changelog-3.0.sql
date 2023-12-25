--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS outbox_messages
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topic   VARCHAR        NOT NULL,
    message VARCHAR        NOT NULL
);
--rollback DROP TABLE outbox_messages;
