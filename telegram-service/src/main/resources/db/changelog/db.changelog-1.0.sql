--liquibase formatted sql

--changeset steshabolk:1
CREATE TABLE IF NOT EXISTS chats
(
    id                  BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    chat_id             BIGINT      UNIQUE NOT NULL,
    user_id             BIGINT      UNIQUE NOT NULL
);
--rollback DROP TABLE chats;
