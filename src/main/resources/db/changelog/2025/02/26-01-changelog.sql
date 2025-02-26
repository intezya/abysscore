-- liquibase formatted sql

-- changeset Tim:1740581580699-1
CREATE TABLE admins
(
    id           BIGINT                      NOT NULL,
    telegram_id  BIGINT                      NOT NULL,
    access_level INTEGER                     NOT NULL,
    admin_from   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_admins PRIMARY KEY (id)
);

-- changeset Tim:1740581580699-2
ALTER TABLE admins
    ADD CONSTRAINT uc_admins_telegramid UNIQUE (telegram_id);

-- changeset Tim:1740581580699-3
ALTER TABLE admins
    ADD CONSTRAINT FK_ADMINS_ON_ID FOREIGN KEY (id) REFERENCES users (id);
