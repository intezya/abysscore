-- liquibase formatted sql

-- changeset intezya:1743523124248-3
ALTER TABLE users
    ADD avatar_url VARCHAR(255);
